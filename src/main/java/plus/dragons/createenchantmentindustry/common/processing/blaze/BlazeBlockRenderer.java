/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.blaze;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer.BlazeBurnerRenderData;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer.HatRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Renderer-state implementation shared by CEI's three live-blaze machines. */
public class BlazeBlockRenderer<T extends BlazeBlockEntity>
        extends SmartBlockEntityRenderer<T, BlazeBlockRenderer.BlazeRenderState> {

    public BlazeBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BlazeRenderState createRenderState() {
        return new BlazeRenderState();
    }

    @Override
    public void extractRenderState(
            T blockEntity,
            BlazeRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.blaze = null;
        HeatLevel heatLevel = blockEntity.getHeatLevelFromBlock();
        if (heatLevel == HeatLevel.NONE)
            return;

        float animation = blockEntity.headAnimation.getValue(tickProgress) * .175f;
        float horizontalAngle = AngleHelper.rad(blockEntity.headAngle.getValue(tickProgress));
        boolean active = animation > .125f && heatLevel.isAtLeast(HeatLevel.FADING);
        PartialModel hat = blockEntity.getHatModelForRender(heatLevel);
        PartialModel goggles = blockEntity.getGogglesModelForRender(heatLevel);
        BlazeBurnerRenderData data = BlazeBurnerRenderer.getBlazeBurnerRenderData(
                blockEntity.getLevel(),
                state.blockState,
                heatLevel,
                animation,
                horizontalAngle,
                active,
                false,
                null,
                blockEntity.hashCode());
        if (hat != null) {
            // CEI supplies separate full-size and small hat models. The Create
            // helper's inert-blaze scaling is intended for one shared hat model;
            // applying it here shrinks and lowers CEI's small model a second time.
            HatRenderState hatState = new HatRenderState();
            hatState.model = CachedBuffers.partial(hat, state.blockState);
            hatState.layer = getHatRenderType();
            hatState.angle = horizontalAngle + Mth.PI;
            hatState.offset = data.headY + .75f;
            hatState.scale = false;
            data.hat = hatState;
        }
        if (goggles != null) {
            data.goggles = CachedBuffers.partial(goggles, state.blockState);
            data.gogglesHeadY = data.headY + .5f;
        }
        state.blaze = data;
    }

    protected RenderType getHatRenderType() {
        return RenderTypes.solidMovingBlock();
    }

    @Override
    public void submit(
            BlazeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        submitBehaviours(state, poseStack, queue, cameraState);
        submitBlaze(state, poseStack, queue);
    }

    protected void submitBehaviours(
            BlazeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        super.submit(state, poseStack, queue, cameraState);
    }

    protected void submitBlaze(BlazeRenderState state, PoseStack poseStack, SubmitNodeCollector queue) {
        if (state.blaze != null)
            state.blaze.render(poseStack, queue);
    }

    public static class BlazeRenderState extends SmartRenderState {
        public @Nullable BlazeBurnerRenderData blaze;
        public final List<FloatingItem> floatingItems = new ArrayList<>();
        public @Nullable ItemStackRenderState valueBoxItem;
        public @Nullable ValueBoxTransform valueBoxTransform;
        public @Nullable BookModel.State book;
        public float bookAngle;
        public float bookYOffset;
    }

    public record FloatingItem(ItemStackRenderState item, float height, float xRot, float zRot) {}
}
