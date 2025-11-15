package com.nolyn.heavystorms.client;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.ModBlockEntities;
import com.nolyn.heavystorms.client.renderer.LightningCapacitorRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HeavyStorms.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHTNING_CAPACITOR_BE.get(), LightningCapacitorRenderer::new);
    }
}
