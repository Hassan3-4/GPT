/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class GrindstoneDrainRenderer
        extends KineticBlockEntityRenderer<GrindstoneDrainBlockEntity, GrindstoneDrainRenderer.GrindstoneRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GrindstoneDrainRenderer(Context context) {
        super(context);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public GrindstoneRenderState createRenderState() {
        return new GrindstoneRenderState();
    }

    @Override
    protected BlockState getRenderedBlockState(GrindstoneDrainBlockEntity blockEntity) {
        return CEIBlocks.MECHANICAL_GRINDSTONE.get().defaultBlockState()
                .setValue(RotatedPillarKineticBlock.AXIS, getRotationAxisOf(blockEntity));
    }

    @Override
    public void extractRenderState(
            GrindstoneDrainBlockEntity drain,
            GrindstoneRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(drain, state, tickProgress, cameraPos, crumblingOverlay);
        state.items.clear();
        state.fluid = null;
        if (state.support)
            updateBaseRenderState(drain, state, drain.getLevel(), crumblingOverlay);

        state.alongZ = drain.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
                .getAxis() == Direction.Axis.Z;
        float duration = drain.inventory.recipeDuration;
        boolean moving = duration != 0;
        float offset = moving ? drain.inventory.remainingTime / duration : 0;
        float processingSpeed = Mth.clamp(drain.getRelativeSpeed() / 32, 1, 128);
        if (moving) {
            offset = Mth.clamp(offset + (-tickProgress + .5f) * processingSpeed / duration, .125f, 1);
            if (!drain.inventory.appliedRecipe)
                offset += 1;
            offset /= 2;
        }
        if (drain.getSpeed() == 0)
            offset = .5f;
        else if (drain.getSpeed() < 0 ^ state.alongZ)
            offset = 1 - offset;
        state.itemOffset = offset;
        state.itemYOffset = Mth.lerp(Mth.sin(offset * Mth.PI), 13 / 16f, 1);

        int outputCount = 0;
        for (int slot = 1; slot < drain.inventory.getContainerSize(); slot++)
            if (!drain.inventory.getItem(slot).isEmpty())
                outputCount++;
        state.outputCount = outputCount;
        int renderedOutput = 0;
        for (int slot = 0; slot < drain.inventory.getContainerSize(); slot++) {
            var stack = drain.inventory.getItem(slot);
            if (stack.isEmpty())
                continue;
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(
                    item, stack, ItemDisplayContext.FIXED, drain.getLevel(), null, drain.hashCode() + slot);
            float lateral = slot > 0 && outputCount > 1
                    ? (.5f / (outputCount - 1)) * renderedOutput
                    : 0;
            state.items.add(new ItemData(item, lateral, slot));
            if (slot > 0)
                renderedOutput++;
        }

        if (drain.tank != null) {
            var primary = drain.tank.getPrimaryTank();
            var stack = primary.getRenderedFluid();
            float fluidLevel = primary.getFluidLevel().getValue(tickProgress);
            if (!stack.isEmpty() && fluidLevel != 0) {
                state.fluid = stack.getFluid();
                state.fluidComponents = stack.directComponents().asPatch();
                state.fluidMaxY = 5 / 16f + fluidLevel * (7 / 16f);
            }
        }
    }

    @Override
    public void submit(
            GrindstoneRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        if (!state.items.isEmpty()) {
            poseStack.pushPose();
            if (state.alongZ)
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.translate(state.outputCount <= 1 ? .5f : .25f, 0, 1 - state.itemOffset);
            poseStack.translate(state.alongZ ? -1 : 0, 0, 0);
            for (ItemData data : state.items) {
                poseStack.pushPose();
                poseStack.translate(0, state.itemYOffset + (data.item.usesBlockLight() ? .1125f : 0), 0);
                if (data.slot > 0 && state.outputCount > 1) {
                    poseStack.translate(data.lateral, 0, 0);
                    TransformStack.of(poseStack).nudge(data.slot * 133);
                }
                poseStack.scale(.5f, .5f, .5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                data.item.submit(poseStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        if (state.fluid != null)
            queue.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), state::renderFluid);
        if (!state.support)
            super.submit(state, poseStack, queue, cameraState);
    }

    public static class GrindstoneRenderState extends KineticRenderState {
        public final List<ItemData> items = new ArrayList<>();
        public boolean alongZ;
        public int outputCount;
        public float itemOffset;
        public float itemYOffset;
        public @Nullable Fluid fluid;
        public DataComponentPatch fluidComponents = DataComponentPatch.EMPTY;
        public float fluidMaxY;

        public void renderFluid(PoseStack.Pose pose, VertexConsumer consumer) {
            FluidRenderHelper.renderFluidBox(
                    fluid,
                    fluidComponents,
                    2 / 16f,
                    5 / 16f,
                    2 / 16f,
                    14 / 16f,
                    fluidMaxY,
                    14 / 16f,
                    consumer,
                    pose,
                    lightCoords,
                    false,
                    false);
        }
    }

    public record ItemData(ItemStackRenderState item, float lateral, int slot) {}
}
