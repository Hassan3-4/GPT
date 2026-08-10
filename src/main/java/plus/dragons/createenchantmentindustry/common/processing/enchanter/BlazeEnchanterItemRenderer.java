/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** 1.21.11 item-model replacement for the former custom BEWLR renderer. */
public class BlazeEnchanterItemRenderer implements ItemModel, SpecialModelRenderer<BlazeEnchanterItemRenderer.HatLayer> {
    public static final Identifier ID = CEICommon.asResource("model/blaze_apparatus");

    private final RenderType renderType = Sheets.translucentBlockItemSheet();
    private final ModelPart base;
    private final ModelPart hat;

    public BlazeEnchanterItemRenderer(ModelPart base, ModelPart hat) {
        this.base = base;
        this.hat = hat;
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
        FoilType foil = stack.hasFoil() ? FoilType.STANDARD : FoilType.NONE;
        appendLayer(state, displayContext, base, false, foil);
        appendLayer(state, displayContext, hat, true, foil);
    }

    private void appendLayer(
            ItemStackRenderState state,
            ItemDisplayContext displayContext,
            ModelPart part,
            boolean special,
            FoilType foil) {
        LayerRenderState layer = state.newLayer();
        layer.setRenderType(renderType);
        layer.setExtents(part.extents);
        layer.setFoilType(foil);
        part.properties.applyToLayer(layer, displayContext);
        layer.prepareQuadList().addAll(part.quads);
        if (special)
            layer.setupSpecialModel(this, new HatLayer(part.quads, renderType, foil));
    }

    @Override
    public void submit(
            HatLayer layer,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            int light,
            int overlay,
            boolean glint,
            int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(.5f, .75f, .5f);
        queue.submitItem(
                poseStack,
                displayContext,
                light,
                overlay,
                outlineColor,
                new int[0],
                layer.quads,
                layer.renderType,
                layer.foilType);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable HatLayer extractArgument(ItemStack stack) {
        return null;
    }

    public record HatLayer(List<BakedQuad> quads, RenderType renderType, FoilType foilType) {}

    private record ModelPart(
            List<BakedQuad> quads,
            ModelRenderProperties properties,
            Supplier<Vector3fc[]> extents) {
        static ModelPart bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new ModelPart(
                    quads,
                    properties,
                    Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads)));
        }
    }

    public record Unbaked(Identifier base, Identifier hat) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                Identifier.CODEC.fieldOf("hat").forGetter(Unbaked::hat)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(base);
            resolver.markDependency(hat);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            return new BlazeEnchanterItemRenderer(ModelPart.bake(baker, base), ModelPart.bake(baker, hat));
        }
    }
}
