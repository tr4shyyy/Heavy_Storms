package com.nolyn.heavystorms.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HeavyStormsConfig {
    public static final ModConfigSpec SERVER_SPEC;

    public static final ModConfigSpec.IntValue EXTRA_LIGHTNING_ATTEMPTS;
    public static final ModConfigSpec.DoubleValue EXTRA_LIGHTNING_CHANCE;
    public static final ModConfigSpec.IntValue LIGHTNING_RADIUS;
    public static final ModConfigSpec.IntValue CAPACITOR_CAPACITY;
    public static final ModConfigSpec.IntValue CAPACITOR_MAX_RECEIVE;
    public static final ModConfigSpec.IntValue CAPACITOR_MAX_EXTRACT;
    public static final ModConfigSpec.IntValue CAPACITOR_CHARGE_PER_STRIKE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Controls the intensity of thunder storms.").push("lightning");
        EXTRA_LIGHTNING_ATTEMPTS = builder
                .comment("How many extra attempts to spawn lightning should be made each server tick while thundering.")
                .defineInRange("extraAttemptsPerTick", 1, 0, 100);
        EXTRA_LIGHTNING_CHANCE = builder
                .comment("Chance (0.0-1.0) that an extra lightning strike attempt actually spawns a bolt.")
                .defineInRange("extraStrikeChance", 0.05D, 0.0D, 1.0D);
        LIGHTNING_RADIUS = builder
                .comment("Maximum horizontal distance (in blocks) from a nearby player where extra lightning may be spawned.")
                .defineInRange("extraStrikeRadius", 128, 8, 256);
        builder.pop();

        builder.comment("Lightning capacitor settings.").push("capacitor");
        CAPACITOR_CAPACITY = builder
                .comment("Total FE the Lightning Capacitor can store.")
                .defineInRange("capacity", 1_000_000, 1_000, Integer.MAX_VALUE);
        CAPACITOR_MAX_RECEIVE = builder
                .comment("Maximum FE the capacitor can receive per operation from automation.")
                .defineInRange("maxReceive", 20000, 1, Integer.MAX_VALUE);
        CAPACITOR_MAX_EXTRACT = builder
                .comment("Maximum FE the capacitor can output per operation to automation.")
                .defineInRange("maxExtract", 20000, 1, Integer.MAX_VALUE);
        CAPACITOR_CHARGE_PER_STRIKE = builder
                .comment("How much FE a single lightning strike generates inside the capacitor.")
                .defineInRange("chargePerStrike", 500000, 1, Integer.MAX_VALUE);
        builder.pop();

        SERVER_SPEC = builder.build();
    }

    private HeavyStormsConfig() {}
}
