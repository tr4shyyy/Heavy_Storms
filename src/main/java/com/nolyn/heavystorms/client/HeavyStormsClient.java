package com.nolyn.heavystorms.client;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.HeavyStormsBlockEntities;
import com.nolyn.heavystorms.client.render.LightningCapacitorRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = HeavyStorms.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HeavyStormsClient {
    private static final ModelResourceLocation LED_MODEL = ModelResourceLocation.standalone(HeavyStorms.id("block/lightning_capacitor_led"));
    private static final ModelResourceLocation GLOW_MODEL = ModelResourceLocation.standalone(HeavyStorms.id("block/lightning_capacitor_glow"));

    private HeavyStormsClient() {}

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(
                HeavyStormsBlockEntities.LIGHTNING_CAPACITOR.get(),
                LightningCapacitorRenderer::new
        ));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(LED_MODEL);
        event.register(GLOW_MODEL);
    }
}
