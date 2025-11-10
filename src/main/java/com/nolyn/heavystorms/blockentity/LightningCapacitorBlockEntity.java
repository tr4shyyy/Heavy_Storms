package com.nolyn.heavystorms.blockentity;

import com.nolyn.heavystorms.config.HeavyStormsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
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
    private static final int STRIKE_FLASH_DURATION_TICKS = 40;

    private int energy;
    private int strikeTicks;

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

    public static void tick(Level level, BlockPos pos, BlockState state, LightningCapacitorBlockEntity blockEntity) {
        blockEntity.tick(level);
    }

    private void tick(Level level) {
        if (strikeTicks > 0) {
            strikeTicks--;
            if (!level.isClientSide && strikeTicks == 0) {
                setChanged();
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getInt(ENERGY_NBT_KEY);
        strikeTicks = tag.getInt(STRIKE_TICKS_NBT_KEY);
    }
}
