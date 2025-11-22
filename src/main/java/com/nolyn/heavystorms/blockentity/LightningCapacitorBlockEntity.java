package com.nolyn.heavystorms.blockentity;

import com.nolyn.heavystorms.config.HeavyStormsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class LightningCapacitorBlockEntity extends BlockEntity {
    private static final String ENERGY_NBT_KEY = "Energy";
    private static final String STRIKE_TICKS_NBT_KEY = "StrikeTicks";
    private static final String CONNECTION_TICKS_NBT_KEY = "ConnectionTicks";
    private static final String GLOW_INTENSITY_NBT_KEY = "GlowIntensity";
    private static final int STRIKE_FLASH_DURATION_TICKS = 40;
    private static final int CONNECTION_FLASH_DURATION_TICKS = 10;

    private int energy;
    private int strikeTicks;
    private int connectionFlashTicks;
    private float prevGlowIntensity;
    private float glowIntensity;
    private boolean ledsVisible;

    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive <= 0) {
                return 0;
            }
            int capacity = getCapacity();
            int energyStored = getEnergyStoredInternal();
            int receiveLimit = Math.min(HeavyStormsConfig.CAPACITOR_MAX_RECEIVE.get(), maxReceive);
            int energyAccepted = Math.min(capacity - energyStored, receiveLimit);
            if (energyAccepted <= 0) {
                return 0;
            }
            if (!simulate) {
                energy = energyStored + energyAccepted;
                connectionFlashTicks = CONNECTION_FLASH_DURATION_TICKS;
                setChangedAndNotify();
            }
            return energyAccepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract <= 0) {
                return 0;
            }
            int energyStored = getEnergyStoredInternal();
            int extractLimit = Math.min(HeavyStormsConfig.CAPACITOR_MAX_EXTRACT.get(), maxExtract);
            int energyRemoved = Math.min(energyStored, extractLimit);
            if (energyRemoved <= 0) {
                return 0;
            }
            if (!simulate) {
                energy = energyStored - energyRemoved;
                connectionFlashTicks = CONNECTION_FLASH_DURATION_TICKS;
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

    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> storage);

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
        connectionFlashTicks = CONNECTION_FLASH_DURATION_TICKS;
        removeNearbyFire();
        setChangedAndNotify();
        return energyAccepted;
    }

    public boolean isRecentlyStruck() {
        return strikeTicks > 0;
    }

    public float getStrikeIntensity(float partialTick) {
        if (strikeTicks <= 0) {
            return 0.0F;
        }
        return Math.max(0.0F, (strikeTicks - partialTick) / (float) STRIKE_FLASH_DURATION_TICKS);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LightningCapacitorBlockEntity blockEntity) {
        blockEntity.tick(level);
    }

    private void tick(Level level) {
        prevGlowIntensity = glowIntensity;
        if (strikeTicks > 0) {
            strikeTicks--;
        }
        if (connectionFlashTicks > 0) {
            connectionFlashTicks--;
        }
        float targetGlow = strikeTicks > 0
                ? 1.0F
                : (connectionFlashTicks > 0 ? 0.6F : 0.0F);
        glowIntensity = Mth.clamp(Mth.lerp(0.25F, glowIntensity, targetGlow), 0.0F, 1.0F);
        boolean nextLedsVisible = shouldDisplayLeds();

        if (level.isClientSide) {
            ledsVisible = nextLedsVisible;
            return;
        }

        boolean glowTurnedOn = prevGlowIntensity <= 0.001F && glowIntensity > 0.001F;
        boolean glowTurnedOff = prevGlowIntensity > 0.001F && glowIntensity <= 0.001F;
        boolean ledsChanged = ledsVisible != nextLedsVisible;
        ledsVisible = nextLedsVisible;
        if (glowTurnedOn || glowTurnedOff || ledsChanged) {
            setChangedAndNotify();
        } else if (strikeTicks == 0 && connectionFlashTicks == 0 && glowIntensity == 0.0F) {
            setChanged();
        }
    }

    public float getGlowIntensity(float partialTick) {
        return Mth.lerp(partialTick, prevGlowIntensity, glowIntensity);
    }

    public boolean getLedVisible() {
        return shouldDisplayLeds();
    }

    private boolean shouldDisplayLeds() {
        return getEnergyStoredInternal() > 0 || strikeTicks > 0 || connectionFlashTicks > 0;
    }

    private void removeNearbyFire() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockPos above = worldPosition.above();
        if (level.getBlockState(above).is(Blocks.FIRE)) {
            level.removeBlock(above, false);
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> storage);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(ENERGY_NBT_KEY, getEnergyStoredInternal());
        tag.putInt(STRIKE_TICKS_NBT_KEY, strikeTicks);
        tag.putInt(CONNECTION_TICKS_NBT_KEY, connectionFlashTicks);
        tag.putFloat(GLOW_INTENSITY_NBT_KEY, glowIntensity);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getInt(ENERGY_NBT_KEY);
        strikeTicks = tag.getInt(STRIKE_TICKS_NBT_KEY);
        connectionFlashTicks = tag.getInt(CONNECTION_TICKS_NBT_KEY);
        glowIntensity = tag.getFloat(GLOW_INTENSITY_NBT_KEY);
        prevGlowIntensity = glowIntensity;
        ledsVisible = shouldDisplayLeds();
    }
}
