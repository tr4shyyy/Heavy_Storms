package com.nolyn.heavystorms.world;

import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class LightningStrikeUtil {
    private LightningStrikeUtil() {}

    public static void triggerGlowFlash(Level level, BlockPos pos) {
        if (level == null) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof LightningCapacitorBlockEntity capacitor) {
            capacitor.triggerGlowFlash();
        }
    }
}
