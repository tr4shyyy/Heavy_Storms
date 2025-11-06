package com.nolyn.heavystorms.blockentity;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HeavyStormsBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HeavyStorms.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningCapacitorBlockEntity>> LIGHTNING_CAPACITOR =
            BLOCK_ENTITIES.register(
                    "lightning_capacitor",
                    () -> BlockEntityType.Builder.of(LightningCapacitorBlockEntity::new, HeavyStormsBlocks.LIGHTNING_CAPACITOR.get()).build(null)
            );

    private HeavyStormsBlockEntities() {}
}
