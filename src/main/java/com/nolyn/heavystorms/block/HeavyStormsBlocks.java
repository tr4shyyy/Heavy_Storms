package com.nolyn.heavystorms.block;

import com.nolyn.heavystorms.HeavyStorms;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HeavyStormsBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HeavyStorms.MOD_ID);

    public static final RegistryObject<LightningCapacitorBlock> LIGHTNING_CAPACITOR = BLOCKS.register(
            "lightning_capacitor",
            () -> new LightningCapacitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
            )
    );

    private HeavyStormsBlocks() {}
}
