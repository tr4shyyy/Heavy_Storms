package com.nolyn.heavystorms.item;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HeavyStormsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HeavyStorms.MOD_ID);

    public static final RegistryObject<BlockItem> LIGHTNING_CAPACITOR = ITEMS.register(
            "lightning_capacitor",
            () -> new BlockItem(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get(), new Item.Properties())
    );

    private HeavyStormsItems() {}
}
