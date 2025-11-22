package com.nolyn.heavystorms.client;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.HeavyStormsBlockEntities;
import com.nolyn.heavystorms.client.render.blockentity.LightningCapacitorRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.resources.ResourceLocation;

public final class HeavyStormsClient {
    private HeavyStormsClient() {}

    public static void initClientEvents() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HeavyStormsClient::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HeavyStormsClient::onRegisterAdditionalModels);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(HeavyStormsBlockEntities.LIGHTNING_CAPACITOR.get(), LightningCapacitorRenderer::new));
    }

    private static void onRegisterAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(model("lightning_capacitor_base"));
        event.register(model("lightning_capacitor_led"));
        event.register(model("lightning_capacitor_glow"));
    }

    private static ResourceLocation model(String path) {
        return new ResourceLocation(HeavyStorms.MOD_ID, "block/" + path);
    }
}
