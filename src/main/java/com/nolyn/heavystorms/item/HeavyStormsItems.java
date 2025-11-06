package com.nolyn.heavystorms.item;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HeavyStormsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HeavyStorms.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> LIGHTNING_CAPACITOR = ITEMS.register(
            "lightning_capacitor",
            () -> new BlockItem(HeavyStormsBlocks.LIGHTNING_CAPACITOR.get(), new Item.Properties())
    );

    private HeavyStormsItems() {}
}
