/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

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

    public static <R extends StandardProcessingRecipe<?>> RecipeSerializer<R> serializer(
            ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory) {
        return new RecipeSerializer<>(
                ProcessingRecipe.codec(factory, ProcessingRecipeParams.codec(ProcessingRecipeParams::new)),
                ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.streamCodec(ProcessingRecipeParams::new)));
    }
}
