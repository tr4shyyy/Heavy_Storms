package com.nolyn.heavystorms.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class LightningCapacitorRenderer implements BlockEntityRenderer<LightningCapacitorBlockEntity> {
    private static final ModelResourceLocation LED_MODEL = ModelResourceLocation.standalone(HeavyStorms.id("block/lightning_capacitor_led"));
    private static final ModelResourceLocation GLOW_MODEL = ModelResourceLocation.standalone(HeavyStorms.id("block/lightning_capacitor_glow"));

    private final BlockRenderDispatcher blockRenderer;

    public LightningCapacitorRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(LightningCapacitorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (state == null) {
            return;
        }

        if (blockEntity.shouldDisplayLeds()) {
            renderLayer(state, poseStack, bufferSource, LED_MODEL, 1.0F, 1.0F, 1.0F, Sheets.cutoutBlockSheet());
        }

        float glowIntensity = blockEntity.getStrikeIntensity(partialTick);
        if (glowIntensity > 0.0F) {
            renderLayer(state, poseStack, bufferSource, GLOW_MODEL, glowIntensity, glowIntensity, glowIntensity, Sheets.translucentCullBlockSheet());
        }
    }

    private void renderLayer(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, ModelResourceLocation modelLocation, float red, float green, float blue, RenderType renderType) {
        ModelManager modelManager = blockRenderer.getBlockModelShaper().getModelManager();
        BakedModel model = modelManager.getModel(modelLocation);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        ModelBlockRenderer modelRenderer = blockRenderer.getModelRenderer();
        modelRenderer.renderModel(poseStack.last(), consumer, state, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
