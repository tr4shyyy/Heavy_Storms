package com.nolyn.heavystorms;

import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import com.nolyn.heavystorms.blockentity.HeavyStormsBlockEntities;
import com.nolyn.heavystorms.capability.HeavyStormsCapabilities;
import com.nolyn.heavystorms.config.HeavyStormsConfig;
import com.nolyn.heavystorms.item.HeavyStormsItems;
import com.nolyn.heavystorms.world.HeavyStormsEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(HeavyStorms.MOD_ID)
public final class HeavyStorms {
    public static final String MOD_ID = "heavy_storms";

    public HeavyStorms(IEventBus modEventBus, ModContainer modContainer) {
        HeavyStormsBlocks.BLOCKS.register(modEventBus);
        HeavyStormsBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        HeavyStormsItems.ITEMS.register(modEventBus);

        modEventBus.addListener(HeavyStorms::onCommonSetup);
        modEventBus.addListener(this::addCreativeTabContents);
        modEventBus.addListener(HeavyStormsCapabilities::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(HeavyStormsEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(HeavyStormsEvents::onLightningSpawn);

        modContainer.registerConfig(ModConfig.Type.SERVER, HeavyStormsConfig.SERVER_SPEC, "heavy_storms-server.toml");
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        // Currently unused, kept for future common setup hooks.
    }

    private void addCreativeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HeavyStormsItems.LIGHTNING_CAPACITOR.get());
        }
    }
}
