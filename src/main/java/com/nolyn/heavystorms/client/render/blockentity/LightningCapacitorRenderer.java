package com.nolyn.heavystorms.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.nolyn.heavystorms.HeavyStorms;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class LightningCapacitorRenderer implements BlockEntityRenderer<LightningCapacitorBlockEntity> {
    private final ModelBlockRenderer modelRenderer;
    private final ResourceLocation baseModelLocation;
    private final ResourceLocation ledModelLocation;
    private final ResourceLocation glowModelLocation;
    private final Logger logger = LogUtils.getLogger();
    private boolean baseMissingLogged = false;
    private boolean ledMissingLogged = false;
    private boolean glowMissingLogged = false;

    public LightningCapacitorRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        this.modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
        this.baseModelLocation = model("block/lightning_capacitor_base");
        this.ledModelLocation = model("block/lightning_capacitor_led");
        this.glowModelLocation = model("block/lightning_capacitor_glow");
    }

    @Override
    public void render(LightningCapacitorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        if (state == null) {
            return;
        }

        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BakedModel baseModel = modelManager.getModel(baseModelLocation);
        logIfMissing(modelManager, baseModel, baseModelLocation, () -> baseMissingLogged, () -> baseMissingLogged = true);
        renderModel(poseStack, buffer.getBuffer(RenderType.cutout()), state, baseModel, 1.0F, 1.0F, 1.0F, packedLight);

        if (blockEntity.getLedVisible()) {
            BakedModel ledModel = modelManager.getModel(ledModelLocation);
            logIfMissing(modelManager, ledModel, ledModelLocation, () -> ledMissingLogged, () -> ledMissingLogged = true);
            renderModel(poseStack, buffer.getBuffer(RenderType.cutout()), state, ledModel, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT);
        }

        float glow = blockEntity.getGlowIntensity(partialTick);
        if (glow > 0.0F) {
            float clamped = Mth.clamp(glow, 0.0F, 1.0F);
            BakedModel glowModel = modelManager.getModel(glowModelLocation);
            logIfMissing(modelManager, glowModel, glowModelLocation, () -> glowMissingLogged, () -> glowMissingLogged = true);
            renderModel(poseStack, buffer.getBuffer(RenderType.translucent()), state, glowModel, clamped, clamped, clamped, LightTexture.FULL_BRIGHT);
        }
    }

    private void renderModel(PoseStack poseStack, VertexConsumer consumer, BlockState state, BakedModel model, float red, float green, float blue, int packedLight) {
        Pose pose = poseStack.last();
        modelRenderer.renderModel(pose, consumer, state, model, red, green, blue, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private static ResourceLocation model(String key) {
        return new ResourceLocation(HeavyStorms.MOD_ID, key);
    }

    private void logIfMissing(ModelManager modelManager, BakedModel model, ResourceLocation location, java.util.function.BooleanSupplier alreadyLogged, Runnable markLogged) {
        if (model == modelManager.getMissingModel() && !alreadyLogged.getAsBoolean()) {
            logger.warn("Lightning capacitor model missing: {}", location);
            markLogged.run();
        }
    }
}
