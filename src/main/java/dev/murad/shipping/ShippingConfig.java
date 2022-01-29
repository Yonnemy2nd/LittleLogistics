package dev.murad.shipping;

import net.minecraftforge.common.ForgeConfigSpec;

public class ShippingConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> fishing_treasure_chance_modifier;
    public static final ForgeConfigSpec.ConfigValue<Double> tug_smoke_modifier;
    public static final ForgeConfigSpec.ConfigValue<Integer> steam_tug_fuel_multiplier;

    static {
        BUILDER.push("general");

        fishing_treasure_chance_modifier = BUILDER.comment("Modify the chance of using the treasure loot table with the auto fishing barge, other factors such as depth and overfishing still play a role.").define("fishingTreasureChance", 0.02);
        steam_tug_fuel_multiplier = BUILDER.comment("Increases the burn duration of Steam tug fuel by N times when compared to furnace, must be an integer. ").define("steamTugFuelMult", 4);
        tug_smoke_modifier = BUILDER.comment("Modify the rate of smoke produced by a tug. Min 0, Max 1, Default 0.4").define("tugSmoke", 0.4);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}