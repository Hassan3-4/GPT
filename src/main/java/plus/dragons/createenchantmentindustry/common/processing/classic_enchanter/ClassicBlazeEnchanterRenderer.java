/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlockRenderer;

public class ClassicBlazeEnchanterRenderer extends BlazeBlockRenderer<ClassicBlazeEnchanterBlockEntity> {
    public static final Material BOOK_MATERIAL =
            new Material(TextureAtlas.LOCATION_BLOCKS, CEICommon.asResource("block/blaze_enchanter_book"));
    private static final float PI = (float) Math.PI;

    private final BookModel bookModel;
    private final TextureAtlasSprite bookTexture;
    private final ItemModelResolver itemModelResolver;

    public ClassicBlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        bookTexture = context.materials().get(BOOK_MATERIAL);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public void extractRenderState(
            ClassicBlazeEnchanterBlockEntity blockEntity,
            BlazeRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.floatingItems.clear();
        state.book = null;

        Level level = blockEntity.getLevel();
        float time = AnimationTickHolder.getRenderTime(level);
        float flip = Mth.lerp(tickProgress, blockEntity.oFlip, blockEntity.flip);
        float page0 = Mth.frac(flip + .25f) * 1.6f - .3f;
        float page1 = Mth.frac(flip + .75f) * 1.6f - .3f;
        state.book = new BookModel.State(
                time,
                Mth.clamp(page0, 0, 1),
                Mth.clamp(page1, 0, 1),
                1);
        state.bookAngle = AngleHelper.rad(blockEntity.headAngle().getValue(tickProgress)) + PI / 2;
        state.bookYOffset = .1f + Mth.sin(time * .1f) * .01f;

        if (!blockEntity.heldItem.isEmpty()) {
            float renderTicks = AnimationTickHolder.getTicks(level);
            float animation = blockEntity.processingTime == -1
                    ? 0
                    : Mth.sin((blockEntity.processingTime + tickProgress) / 20f);
            float height = 1.25f + (1 + animation) * .25f;
            float xRot = (renderTicks * 5 + blockEntity.getBlockPos().getX()) % 360;
            float zRot = (renderTicks * 5 + blockEntity.getBlockPos().getZ()) % 360;
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(
                    item,
                    blockEntity.heldItem,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    blockEntity.hashCode());
            state.floatingItems.add(new FloatingItem(item, height, xRot, zRot));
        }
    }

    @Override
    public void submit(
            BlazeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        submitBehaviours(state, poseStack, queue, cameraState);
        if (state.book != null) {
            poseStack.pushPose();
            poseStack.translate(.5, .25, .5);
            poseStack.translate(0, state.bookYOffset, 0);
            poseStack.mulPose(Axis.YP.rotation(state.bookAngle));
            poseStack.mulPose(Axis.ZP.rotationDegrees(80));
            poseStack.scale(1.2f, 1.2f, 1.2f);
            queue.submitModel(
                    bookModel,
                    state.book,
                    poseStack,
                    BOOK_MATERIAL.renderType(RenderTypes::entitySolid),
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    bookTexture,
                    0,
                    null);
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
        poseStack.pushPose();
        poseStack.translate(0, .2, 0);
        submitBlaze(state, poseStack, queue);
        poseStack.popPose();
    }
}
