package com.nolyn.heavystorms;

import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import com.nolyn.heavystorms.blockentity.HeavyStormsBlockEntities;
import com.nolyn.heavystorms.client.HeavyStormsClient;
import com.nolyn.heavystorms.config.HeavyStormsConfig;
import com.nolyn.heavystorms.item.HeavyStormsItems;
import com.nolyn.heavystorms.world.HeavyStormsEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HeavyStorms.MOD_ID)
public final class HeavyStorms {
    public static final String MOD_ID = "heavy_storms";

    public HeavyStorms() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        HeavyStormsBlocks.BLOCKS.register(modEventBus);
        HeavyStormsBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        HeavyStormsItems.ITEMS.register(modEventBus);

        modEventBus.addListener(HeavyStorms::onCommonSetup);
        modEventBus.addListener(this::addCreativeTabContents);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> HeavyStormsClient::initClientEvents);

        MinecraftForge.EVENT_BUS.addListener(HeavyStormsEvents::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(HeavyStormsEvents::onLightningSpawn);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HeavyStormsConfig.SERVER_SPEC, "heavy_storms-server.toml");
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
