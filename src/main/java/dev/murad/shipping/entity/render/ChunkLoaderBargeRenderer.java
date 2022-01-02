package dev.murad.shipping.entity.render;

import dev.murad.shipping.ShippingMod;
import dev.murad.shipping.entity.custom.barge.AbstractBargeEntity;
import dev.murad.shipping.entity.models.ChestBargeModel;
import dev.murad.shipping.entity.models.ChunkLoaderBargeModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;

public class ChunkLoaderBargeRenderer extends AbstractBargeRenderer{
    private static final ResourceLocation BARGE_TEXTURE =
            new ResourceLocation(ShippingMod.MOD_ID, "textures/entity/chunk_loader_barge.png");

    private final EntityModel model = new ChunkLoaderBargeModel();


    public ChunkLoaderBargeRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractBargeEntity entity) {
        return BARGE_TEXTURE;
    }

    @Override
    public EntityModel getModel() {
        return model;
    }
}
