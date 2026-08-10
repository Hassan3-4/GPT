/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlockRenderer;

public class BlazeForgerRenderer extends BlazeBlockRenderer<BlazeForgerBlockEntity> {
    private final ItemModelResolver itemModelResolver;

    public BlazeForgerRenderer(Context context) {
        super(context);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    protected RenderType getHatRenderType() {
        return RenderTypes.cutoutMovingBlock();
    }

    @Override
    public void extractRenderState(
            BlazeForgerBlockEntity blockEntity,
            BlazeRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.floatingItems.clear();
        Level level = blockEntity.getLevel();
        float renderTicks = AnimationTickHolder.getTicks(level) + tickProgress;
        for (int slot = 0; slot < 4; slot++) {
            var stack = blockEntity.inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            float animation = blockEntity.processingTime == -1
                    ? Mth.sin(slot * Mth.PI / -2f)
                    : Mth.sin((blockEntity.processingTime + tickProgress) / 20f + slot * Mth.PI);
            float height = 1.25f + (1 + animation) * .25f;
            float xRot = (renderTicks * 5 + blockEntity.getBlockPos().getX() + slot * 180) % 360;
            float zRot = (renderTicks * 5 + blockEntity.getBlockPos().getZ() + slot * 180) % 360;
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(
                    item, stack, ItemDisplayContext.FIXED, level, null, blockEntity.hashCode() + slot);
            state.floatingItems.add(new FloatingItem(item, height, xRot, zRot));
        }
    }

    @Override
    public void submit(
            BlazeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        for (FloatingItem floating : state.floatingItems) {
            poseStack.pushPose();
            poseStack.translate(.5f, floating.height(), .5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(floating.xRot()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(floating.zRot()));
            poseStack.scale(.5f, .5f, .5f);
            floating.item().submit(poseStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        super.submit(state, poseStack, queue, cameraState);
    }
}
