package com.nolyn.heavystorms.world;

import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import com.nolyn.heavystorms.config.HeavyStormsConfig;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;

public final class HeavyStormsEvents {
    private static final Set<LightningBolt> TRACKED_LIGHTNING = Collections.newSetFromMap(new WeakHashMap<LightningBolt, Boolean>());

    private HeavyStormsEvents() {}

    public static void onLevelTick(final LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        processTrackedLightning(serverLevel);
        if (!serverLevel.isThundering()) {
            return;
        }
        List<ServerPlayer> players = serverLevel.players();
        if (players.isEmpty()) {
            return;
        }

        int attempts = HeavyStormsConfig.EXTRA_LIGHTNING_ATTEMPTS.get();
        double chance = HeavyStormsConfig.EXTRA_LIGHTNING_CHANCE.get();
        for (int i = 0; i < attempts; i++) {
            if (serverLevel.random.nextDouble() <= chance) {
                spawnExtraLightning(serverLevel, players);
            }
        }
    }

    private static void processTrackedLightning(ServerLevel level) {
        Iterator<LightningBolt> iterator = TRACKED_LIGHTNING.iterator();
        while (iterator.hasNext()) {
            LightningBolt lightning = iterator.next();
            Level lightningLevel = lightning.level();
            if (!(lightningLevel instanceof ServerLevel lightningServerLevel)) {
                iterator.remove();
                continue;
            }
            if (lightningServerLevel != level) {
                if (!lightning.isAlive()) {
                    iterator.remove();
                }
                continue;
            }
            if (!lightning.isAlive()) {
                iterator.remove();
                continue;
            }
            if (lightning.tickCount < 1) {
                continue;
            }

            BlockPos strikePos = BlockPos.containing(lightning.getX(), lightning.getY() - 1.0E-6D, lightning.getZ());
            BlockPos rodPos = findStruckLightningRod(level, strikePos);
            if (rodPos != null) {
                BlockPos capacitorPos = rodPos.below();
                if (level.getBlockState(capacitorPos).is(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get())
                        && level.getBlockEntity(capacitorPos) instanceof LightningCapacitorBlockEntity capacitor) {
                    capacitor.addEnergy(HeavyStormsConfig.CAPACITOR_CHARGE_PER_STRIKE.get());
                }
                iterator.remove();
                continue;
            }
        }
    }

    private static void spawnExtraLightning(ServerLevel level, List<ServerPlayer> players) {
        int radius = HeavyStormsConfig.LIGHTNING_RADIUS.get();
        ServerPlayer anchor = players.get(level.random.nextInt(players.size()));
        BlockPos anchorPos = anchor.blockPosition();

        for (int attempt = 0; attempt < 4; attempt++) {
            int dx = level.random.nextInt(radius * 2 + 1) - radius;
            int dz = level.random.nextInt(radius * 2 + 1) - radius;
            BlockPos candidate = anchorPos.offset(dx, 0, dz);
            BlockPos strikePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            if (!level.isAreaLoaded(strikePos, 1) || !level.canSeeSky(strikePos)) {
                continue;
            }

            if (spawnLightningBolt(level, strikePos)) {
                return;
            }
        }
    }

    private static boolean spawnLightningBolt(ServerLevel level, BlockPos strikePos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return false;
        }

        bolt.moveTo(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
        level.addFreshEntity(bolt);
        return true;
    }

    public static void onLightningSpawn(final EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LightningBolt lightning)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }

        TRACKED_LIGHTNING.add(lightning);
    }

    @Nullable
    private static BlockPos findStruckLightningRod(ServerLevel level, BlockPos strikePos) {
        for (int dy = 1; dy >= -2; dy--) {
            BlockPos checkPos = strikePos.offset(0, dy, 0);
            if (level.getBlockState(checkPos).is(Blocks.LIGHTNING_ROD)) {
                return checkPos;
            }
        }
        return null;
    }
}
