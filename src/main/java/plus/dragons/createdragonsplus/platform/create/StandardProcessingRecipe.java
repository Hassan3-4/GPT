/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Standard item-input processing recipe and its Fabric serializer. */
public abstract class StandardProcessingRecipe<T extends RecipeInput>
        extends ProcessingRecipe<T, ProcessingRecipeParams> {
    protected StandardProcessingRecipe(IRecipeTypeInfo typeInfo, ProcessingRecipeParams params) {
        super(typeInfo, params);
    }

    public static class Builder<R extends StandardProcessingRecipe<?>>
            extends ProcessingRecipeBuilder<ProcessingRecipeParams, R, Builder<R>> {
        public Builder(ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory, Identifier recipeId) {
            super(factory, recipeId);
        }

        @Override
        protected ProcessingRecipeParams createParams() {
            return new ProcessingRecipeParams();
        }

        @Override
        protected Builder<R> self() {
            return this;
        }
    }

    public static class Serializer<R extends StandardProcessingRecipe<?>> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory) {
            codec = ProcessingRecipe.codec(factory, ProcessingRecipeParams.codec(ProcessingRecipeParams::new));
            streamCodec = ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.streamCodec(ProcessingRecipeParams::new));
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
