/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** 1.21.11 item model preserving the animated-book layer of the classic enchanter item. */
public class ClassicBlazeEnchanterItemRenderer implements ItemModel, SpecialModelRenderer<BookModel.State> {
    public static final Identifier ID = CEICommon.asResource("model/classic_blaze_enchanter");
    private static final Material BOOK_MATERIAL =
            new Material(TextureAtlas.LOCATION_BLOCKS, CEICommon.asResource("block/blaze_enchanter_book"));
    private static final BookModel.State BOOK_STATE = new BookModel.State(
            0,
            Mth.clamp(Mth.frac(.25f) * 1.6f - .3f, 0, 1),
            Mth.clamp(Mth.frac(.75f) * 1.6f - .3f, 0, 1),
            1);

    private final RenderType renderType = Sheets.translucentBlockItemSheet();
    private final List<BakedQuad> quads;
    private final ModelRenderProperties properties;
    private final Supplier<Vector3fc[]> extents;
    private final BookModel bookModel;

    public ClassicBlazeEnchanterItemRenderer(
            List<BakedQuad> quads,
            ModelRenderProperties properties,
            BookModel bookModel) {
        this.quads = quads;
        this.properties = properties;
        this.extents = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads));
        this.bookModel = bookModel;
    }

    @Override
    public void update(
            ItemStackRenderState state,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed) {
        state.appendModelIdentityElement(this);

        LayerRenderState base = state.newLayer();
        base.setRenderType(renderType);
        base.setExtents(extents);
        properties.applyToLayer(base, displayContext);
        base.prepareQuadList().addAll(quads);

        LayerRenderState book = state.newLayer();
        book.setExtents(extents);
        properties.applyToLayer(book, displayContext);
        book.setupSpecialModel(this, BOOK_STATE);
    }

    @Override
    public void submit(
            BookModel.State state,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            int light,
            int overlay,
            boolean glint,
            int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0, -.3, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(0, .05, 0);
        poseStack.scale(1.2f, 1.2f, 1.2f);
        queue.submitModel(
                bookModel,
                state,
                poseStack,
                BOOK_MATERIAL.renderType(RenderTypes::entitySolid),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                -1,
                Minecraft.getInstance().getAtlasManager().get(BOOK_MATERIAL),
                outlineColor,
                null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BookModel.State extractArgument(ItemStack stack) {
        return BOOK_STATE;
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(base);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new ClassicBlazeEnchanterItemRenderer(
                    quads,
                    properties,
                    new BookModel(context.entityModelSet().bakeLayer(ModelLayers.BOOK)));
        }
    }
}
