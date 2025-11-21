package com.nolyn.heavystorms.blockentity;

import com.nolyn.heavystorms.config.HeavyStormsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class LightningCapacitorBlockEntity extends BlockEntity {
    private static final String ENERGY_NBT_KEY = "Energy";
    private static final String STRIKE_TICKS_NBT_KEY = "StrikeTicks";
    private static final String CONNECTION_TICKS_NBT_KEY = "ConnectionTicks";
    private static final int STRIKE_FLASH_DURATION_TICKS = 120;
    private static final int CONNECTION_FLASH_DURATION_TICKS = 20;
    private static final int FIRE_CLEAR_RADIUS = 1;

    private int energy;
    private int strikeTicks;
    private int connectionTicks;
    private float glowIntensity;
    private float prevGlowIntensity;

    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive <= 0) {
                return 0;
            }
            flagConnectionActivity();
            int capacity = getCapacity();
            int energyStored = getEnergyStoredInternal();
            int receiveLimit = Math.min(HeavyStormsConfig.CAPACITOR_MAX_RECEIVE.get(), maxReceive);
            int energyAccepted = Math.min(capacity - energyStored, receiveLimit);
            if (energyAccepted <= 0) {
                return 0;
            }
            if (!simulate) {
                energy = energyStored + energyAccepted;
                setChangedAndNotify();
            }
            return energyAccepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract <= 0) {
                return 0;
            }
            flagConnectionActivity();
            int energyStored = getEnergyStoredInternal();
            int extractLimit = Math.min(HeavyStormsConfig.CAPACITOR_MAX_EXTRACT.get(), maxExtract);
            int energyRemoved = Math.min(energyStored, extractLimit);
            if (energyRemoved <= 0) {
                return 0;
            }
            if (!simulate) {
                energy = energyStored - energyRemoved;
                setChangedAndNotify();
            }
            return energyRemoved;
        }

        @Override
        public int getEnergyStored() {
            return getEnergyStoredInternal();
        }

        @Override
        public int getMaxEnergyStored() {
            return getCapacity();
        }

        @Override
        public boolean canExtract() {
            return HeavyStormsConfig.CAPACITOR_MAX_EXTRACT.get() > 0;
        }

        @Override
        public boolean canReceive() {
            return HeavyStormsConfig.CAPACITOR_MAX_RECEIVE.get() > 0;
        }
    };

    public LightningCapacitorBlockEntity(BlockPos pos, BlockState state) {
        this(HeavyStormsBlockEntities.LIGHTNING_CAPACITOR.get(), pos, state);
    }

    protected LightningCapacitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return storage;
    }

    public int addEnergy(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int energyStored = getEnergyStoredInternal();
        int capacity = getCapacity();
        int energyAccepted = Math.min(capacity - energyStored, amount);
        if (energyAccepted <= 0) {
            return 0;
        }
        energy = energyStored + energyAccepted;
        strikeTicks = STRIKE_FLASH_DURATION_TICKS;
        glowIntensity = 1.0F;
        prevGlowIntensity = 1.0F;
        setChangedAndNotify();
        return energyAccepted;
    }

    public boolean isRecentlyStruck() {
        return strikeTicks > 0;
    }

    public float getStrikeIntensity(float partialTick) {
        return Mth.clamp(Mth.lerp(partialTick, prevGlowIntensity, glowIntensity), 0.0F, 1.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LightningCapacitorBlockEntity blockEntity) {
        blockEntity.tick(level);
    }

    private void tick(Level level) {
        prevGlowIntensity = glowIntensity;

        if (strikeTicks > 0) {
            strikeTicks--;
            if (!level.isClientSide && strikeTicks == 0) {
                setChanged();
            }
        }
        if (connectionTicks > 0) {
            connectionTicks--;
            if (!level.isClientSide && connectionTicks == 0) {
                setChangedAndNotify();
            }
        }
        if (!level.isClientSide) {
            extinguishNearbyFire(level);
        }

        float targetGlow = strikeTicks > 0 ? 1.0F : 0.0F;
        float blend = strikeTicks > 0 ? 0.25F : 0.08F; // rise faster than it fades
        glowIntensity = Mth.clamp(glowIntensity + (targetGlow - glowIntensity) * blend, 0.0F, 1.0F);
        if (glowIntensity < 0.001F) {
            glowIntensity = 0.0F;
        }
    }

    private void extinguishNearbyFire(Level level) {
        if (level == null) {
            return;
        }
        BlockPos center = getBlockPos();
        for (int dx = -FIRE_CLEAR_RADIUS; dx <= FIRE_CLEAR_RADIUS; dx++) {
            for (int dz = -FIRE_CLEAR_RADIUS; dz <= FIRE_CLEAR_RADIUS; dz++) {
                for (int dy = 0; dy <= FIRE_CLEAR_RADIUS; dy++) {
                    BlockPos targetPos = center.offset(dx, dy, dz);
                    if (level.getBlockState(targetPos).is(net.minecraft.world.level.block.Blocks.FIRE)) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
        }
    }

    private int getEnergyStoredInternal() {
        int capacity = getCapacity();
        if (energy > capacity) {
            energy = capacity;
            setChangedAndNotify();
        }
        return energy;
    }

    private int getCapacity() {
        return Math.max(0, HeavyStormsConfig.CAPACITOR_CAPACITY.get());
    }

    private void setChangedAndNotify() {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean shouldDisplayLeds() {
        return energy > 0 || connectionTicks > 0;
    }

    private void flagConnectionActivity() {
        if (connectionTicks == 0) {
            connectionTicks = CONNECTION_FLASH_DURATION_TICKS;
            setChangedAndNotify();
        } else {
            connectionTicks = CONNECTION_FLASH_DURATION_TICKS;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(ENERGY_NBT_KEY, getEnergyStoredInternal());
        tag.putInt(STRIKE_TICKS_NBT_KEY, strikeTicks);
        tag.putInt(CONNECTION_TICKS_NBT_KEY, connectionTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt(ENERGY_NBT_KEY);
        strikeTicks = tag.getInt(STRIKE_TICKS_NBT_KEY);
        connectionTicks = tag.getInt(CONNECTION_TICKS_NBT_KEY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        loadAdditional(packet.getTag(), registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
