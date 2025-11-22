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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.Nullable;

public final class HeavyStormsEvents {
    private static final Set<LightningBolt> TRACKED_LIGHTNING = Collections.newSetFromMap(new WeakHashMap<LightningBolt, Boolean>());

    private HeavyStormsEvents() {}

    public static void onLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Level level = event.level;
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
            BlockPos capacitorPos = findStruckCapacitor(level, strikePos);
            if (capacitorPos != null && level.getBlockEntity(capacitorPos) instanceof LightningCapacitorBlockEntity capacitor) {
                capacitor.addEnergy(HeavyStormsConfig.CAPACITOR_CHARGE_PER_STRIKE.get());
                extinguishNearbyFire(level, capacitorPos);
                iterator.remove();
                continue;
            }
            // If we hit near a capacitor but missed the block exactly, still clear fire to protect the structure.
            BlockPos nearbyCapacitor = findNearestCapacitorStrikePos(level, strikePos, 2);
            if (nearbyCapacitor != null) {
                extinguishNearbyFire(level, nearbyCapacitor);
            }
        }
    }

    private static void spawnExtraLightning(ServerLevel level, List<ServerPlayer> players) {
        int radius = HeavyStormsConfig.LIGHTNING_RADIUS.get();
        ServerPlayer anchor = players.get(level.random.nextInt(players.size()));
        BlockPos anchorPos = anchor.blockPosition();

        BlockPos capacitorStrike = findNearestCapacitorStrikePos(level, anchorPos, radius);
        if (capacitorStrike != null && level.random.nextFloat() < 0.12F && spawnLightningBolt(level, capacitorStrike)) {
            return;
        }

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

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos strikePos = BlockPos.containing(lightning.getX(), lightning.getY() - 1.0E-6D, lightning.getZ());
        BlockPos retarget = findNearestCapacitorStrikePos(level, strikePos, 10);
        if (retarget != null && level.random.nextFloat() < 0.05F) { // much softer pull
            lightning.moveTo(retarget.getX() + 0.5D, retarget.getY(), retarget.getZ() + 0.5D);
        }

        TRACKED_LIGHTNING.add(lightning);
    }

    @Nullable
    private static BlockPos findStruckCapacitor(ServerLevel level, BlockPos strikePos) {
        for (int dy = 1; dy >= -2; dy--) {
            BlockPos checkPos = strikePos.offset(0, dy, 0);
            // Prefer a lightning rod sitting on the capacitor, but also allow the capacitor itself to act as the rod.
            if (level.getBlockState(checkPos).is(Blocks.LIGHTNING_ROD)) {
                BlockPos below = checkPos.below();
                if (level.getBlockState(below).is(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get())) {
                    return below;
                }
            } else if (level.getBlockState(checkPos).is(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get())) {
                return checkPos;
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos findNearestCapacitorStrikePos(ServerLevel level, BlockPos origin, int radius) {
        int cappedRadius = Math.min(radius, 12); // further reduce pull radius
        int radiusSq = cappedRadius * cappedRadius;
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int dx = -cappedRadius; dx <= cappedRadius; dx++) {
            for (int dz = -cappedRadius; dz <= cappedRadius; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                if (!level.canSeeSky(top)) {
                    continue;
                }
                // Check top block and the one below for a capacitor.
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos check = top.offset(0, dy, 0);
                    if (level.getBlockState(check).is(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get())) {
                        double dist = check.distSqr(origin);
                        if (dist <= radiusSq && dist < closestDist) {
                            closestDist = dist;
                            BlockPos above = check.above();
                            closest = level.getBlockState(above).is(Blocks.LIGHTNING_ROD) ? above : check;
                        }
                    }
                }
            }
        }

        return closest;
    }

    private static void extinguishNearbyFire(ServerLevel level, BlockPos center) {
        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(Blocks.FIRE)) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}
