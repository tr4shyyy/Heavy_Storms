package com.nolyn.heavystorms.block;

import com.nolyn.heavystorms.HeavyStorms;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HeavyStormsBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, HeavyStorms.MOD_ID);

    public static final DeferredHolder<Block, LightningCapacitorBlock> LIGHTNING_CAPACITOR = BLOCKS.register(
            "lightning_capacitor",
            () -> new LightningCapacitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(4.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.COPPER)
            )
    );

    private HeavyStormsBlocks() {}
}
