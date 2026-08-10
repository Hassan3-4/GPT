/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlockRenderer;

public class BlazeEnchanterRenderer extends BlazeBlockRenderer<BlazeEnchanterBlockEntity> {
    private final ItemModelResolver itemModelResolver;

    public BlazeEnchanterRenderer(Context context) {
        super(context);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public void extractRenderState(
            BlazeEnchanterBlockEntity blockEntity,
            BlazeRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.floatingItems.clear();
        state.valueBoxItem = null;
        state.valueBoxTransform = null;

        Level level = blockEntity.getLevel();
        if (!blockEntity.heldItem.isEmpty()) {
            float renderTicks = AnimationTickHolder.getTicks(level) + tickProgress;
            float animation = blockEntity.processingTime == -1
                    ? 0
                    : Mth.sin((blockEntity.processingTime + tickProgress) / 20f);
            float height = 1.25f + (1 + animation) * .25f;
            float xRot = (renderTicks * 5 + blockEntity.getBlockPos().getX()) % 360;
            float zRot = (renderTicks * 5 + blockEntity.getBlockPos().getZ()) % 360;
            ItemStackRenderState item = createItemState(blockEntity, blockEntity.heldItem);
            state.floatingItems.add(new FloatingItem(item, height, xRot, zRot));
        }

        EnchanterBehaviour behaviour = blockEntity.getEnchanterBehaviour();
        if (behaviour.getTemplate().isEmpty())
            return;
        Minecraft minecraft = Minecraft.getInstance();
        HitResult target = minecraft.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;
        TemplateItemTransform templateItemTransform = new TemplateItemTransform();
        templateItemTransform.fromSide(result.getDirection());
        Vec3 localHit = target.getLocation().subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        if (!templateItemTransform.testHit(level, blockEntity.getBlockPos(), state.blockState, localHit))
            return;
        Entity camera = minecraft.getCameraEntity();
        float maxDistance = AllConfigs.client().filterItemRenderDistance.getF();
        if (!blockEntity.isVirtual()
                && camera != null
                && level == camera.level()
                && camera.position().distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) > maxDistance * maxDistance)
            return;
        state.valueBoxItem = createItemState(blockEntity, behaviour.getTemplate());
        state.valueBoxTransform = templateItemTransform;
    }

    private ItemStackRenderState createItemState(BlazeEnchanterBlockEntity blockEntity, net.minecraft.world.item.ItemStack stack) {
        ItemStackRenderState state = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(
                state, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, blockEntity.hashCode());
        return state;
    }

    private static class TemplateItemTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 14.5);
        }

        @Override
        public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            if (!isSideActive(state, getSide())) return false;
            Vec3 location = VecHelper.voxelSpace(8, 8, 13.5);
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            return localHit.distanceTo(location) < scale * 1.2;
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }

    @Override
    public void submit(
            BlazeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        if (state.valueBoxItem != null
                && state.valueBoxTransform != null
                && state.valueBoxTransform.shouldRender(state.blockState)) {
            poseStack.pushPose();
            state.valueBoxTransform.transform(state.blockState, poseStack);
            ValueBoxRenderer.renderItemIntoValueBox(
                    state.valueBoxItem, queue, poseStack, state.lightCoords, 0);
            poseStack.popPose();
        }
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
