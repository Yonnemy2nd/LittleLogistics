package dev.murad.shipping.entity.custom.train.locomotive;

import dev.murad.shipping.ShippingConfig;
import dev.murad.shipping.capability.ReadWriteEnergyStorage;
import dev.murad.shipping.entity.accessor.DataAccessor;
import dev.murad.shipping.entity.accessor.EnergyLocomotiveDataAccessor;
import dev.murad.shipping.entity.accessor.EnergyTugDataAccessor;
import dev.murad.shipping.entity.container.EnergyLocomotiveContainer;
import dev.murad.shipping.entity.container.EnergyTugContainer;
import dev.murad.shipping.setup.ModEntityTypes;
import dev.murad.shipping.setup.ModItems;
import dev.murad.shipping.util.InventoryUtils;
import dev.murad.shipping.util.ItemHandlerVanillaContainerWrapper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyLocomotiveEntity extends AbstractLocomotiveEntity implements ItemHandlerVanillaContainerWrapper {
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private static final int MAX_ENERGY = ShippingConfig.Server.ENERGY_TUG_BASE_CAPACITY.get();
    private static final int MAX_TRANSFER = ShippingConfig.Server.ENERGY_TUG_BASE_MAX_CHARGE_RATE.get();
    private static final int ENERGY_USAGE = ShippingConfig.Server.ENERGY_TUG_BASE_ENERGY_USAGE.get();

    private final ReadWriteEnergyStorage internalBattery = new ReadWriteEnergyStorage(MAX_ENERGY, MAX_TRANSFER, Integer.MAX_VALUE);
    private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> internalBattery);

    public EnergyLocomotiveEntity(EntityType<?> type, Level p_38088_) {
        super(type, p_38088_);
        internalBattery.setEnergy(0);
    }

    public EnergyLocomotiveEntity(Level level, Double aDouble, Double aDouble1, Double aDouble2) {
        super(ModEntityTypes.ENERGY_LOCOMOTIVE.get(), level, aDouble, aDouble1, aDouble2);
        internalBattery.setEnergy(0);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return holder.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void remove(RemovalReason r) {
        if(!this.level.isClientSide){
            Containers.dropContents(this.level, this, this);
        }
        super.remove(r);
    }



    @Override
    protected MenuProvider createContainerProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent("entity.littlelogistics.energy_locomotive");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player Player) {
                return new EnergyLocomotiveContainer(i, level, getDataAccessor(), playerInventory, Player);
            }
        };
    }

    @Override
    public EnergyLocomotiveDataAccessor getDataAccessor() {
        return (EnergyLocomotiveDataAccessor) new EnergyLocomotiveDataAccessor.Builder(this.getId())
                .withOn(() -> engineOn)
                .withEnergy(internalBattery::getEnergyStored)
                .withCapacity(internalBattery::getMaxEnergyStored)
                .withLit(() -> internalBattery.getEnergyStored() > 0) // has energy
                .build();
    }

    @Override
    public void tick() {
        // grab energy from capacitor
        if (!level.isClientSide) {
            IEnergyStorage capability = InventoryUtils.getEnergyCapabilityInSlot(0, itemHandler);
            if (capability != null) {
                // simulate first
                int toExtract = capability.extractEnergy(MAX_TRANSFER, true);
                toExtract = internalBattery.receiveEnergy(toExtract, false);
                capability.extractEnergy(toExtract, false);
            }
        }

        super.tick();
    }

    @Override
    protected boolean tickFuel() {
        return internalBattery.extractEnergy(ENERGY_USAGE, false) > 0;
    }


    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.ENERGY_LOCOMOTIVE.get());
    }

    @Override
    public ItemStackHandler getRawHandler() {
        return itemHandler;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        itemHandler.deserializeNBT(compound.getCompound("inv"));
        internalBattery.readAdditionalSaveData(compound.getCompound("energy_storage"));
        super.readAdditionalSaveData(compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("inv", itemHandler.serializeNBT());
        CompoundTag energyNBT = new CompoundTag();
        internalBattery.addAdditionalSaveData(energyNBT);
        compound.put("energy_storage", energyNBT);
        super.addAdditionalSaveData(compound);
    }
}
