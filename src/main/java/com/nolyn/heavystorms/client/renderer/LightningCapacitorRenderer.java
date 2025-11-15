package com.nolyn.heavystorms.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import com.nolyn.heavystorms.client.model.LightningCapacitorModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.Color;

public final class LightningCapacitorRenderer extends GeoBlockRenderer<LightningCapacitorBlockEntity> {
    private static final int FULL_BRIGHTNESS = 0xF000F0;

    public LightningCapacitorRenderer(BlockEntityRendererProvider.Context context) {
        super(new LightningCapacitorModel());
    }

    @Override
    public RenderType getRenderType(LightningCapacitorBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(LightningCapacitorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        float alpha = blockEntity.getGlowIntensity();
        if (alpha <= 0.0F) {
            return;
        }

        LightningCapacitorModel model = (LightningCapacitorModel) getGeoModel();
        RenderType emissive = RenderType.entityTranslucentEmissive(model.getEmissiveTexture(blockEntity));
        BakedGeoModel bakedModel = model.getBakedModel(model.getModelResource(blockEntity));
        int emissiveColor = Color.ofRGBA(1.0F, 1.0F, 1.0F, alpha).getColor();

        reRender(
                bakedModel,
                poseStack,
                bufferSource,
                blockEntity,
                emissive,
                bufferSource.getBuffer(emissive),
                partialTick,
                FULL_BRIGHTNESS,
                OverlayTexture.NO_OVERLAY,
                emissiveColor
        );
    }
}
