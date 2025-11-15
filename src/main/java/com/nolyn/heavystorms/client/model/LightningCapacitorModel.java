package com.nolyn.heavystorms.client.model;

import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;


public final class LightningCapacitorModel extends GeoModel<LightningCapacitorBlockEntity> {
    private static final ResourceLocation MODEL = path("geo/lightning_capacitor.geo.json");
    private static final ResourceLocation BASE_TEXTURE = path("textures/block/lightning_capacitor_base.png");
    private static final ResourceLocation EMISSIVE_TEXTURE = path("textures/block/lightning_capacitor_flash.png");
    private static final ResourceLocation ANIMATION = path("animations/lightning_capacitor.animation.json");

    private static ResourceLocation path(String relativePath) {
        return ResourceLocation.fromNamespaceAndPath(HeavyStorms.MOD_ID, relativePath);
    }
    @Override
    public RenderType getRenderType(LightningCapacitorBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }


    @Override
    public ResourceLocation getModelResource(LightningCapacitorBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LightningCapacitorBlockEntity animatable) {
        return BASE_TEXTURE;
    }

    public ResourceLocation getEmissiveTexture(LightningCapacitorBlockEntity animatable) {
        return EMISSIVE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LightningCapacitorBlockEntity animatable) {
        return ANIMATION;
    }
}
