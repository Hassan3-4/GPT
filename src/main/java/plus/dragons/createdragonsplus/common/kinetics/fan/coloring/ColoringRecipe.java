/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import plus.dragons.createdragonsplus.platform.create.ProcessingRecipe;
import plus.dragons.createdragonsplus.platform.create.ProcessingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;

public class ColoringRecipe extends ProcessingRecipe<ColoringRecipeInput, ColoringRecipeParams> {
    public ColoringRecipe(ColoringRecipeParams params) {
        super(CDPRecipes.COLORING, params);
    }

    public static Builder builder(Identifier id, Identifier color) {
        return new Builder(id, color);
    }

    public Identifier getColor() {
        return params.color;
    }

    @Override
    public boolean matches(ColoringRecipeInput input, Level level) {
        return params.color.equals(input.color()) && this.ingredients.getFirst().test(input.item());
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    public static class Builder extends ProcessingRecipeBuilder<ColoringRecipeParams, ColoringRecipe, Builder> {
        protected Builder(Identifier recipeId, Identifier color) {
            super(ColoringRecipe::new, recipeId);
            this.params.color = color;
        }

        @Override
        protected ColoringRecipeParams createParams() {
            return new ColoringRecipeParams();
        }

        @Override
        public Builder self() {
            return this;
        }
    }

    public static <R extends ColoringRecipe> RecipeSerializer<R> serializer(
            ProcessingRecipe.Factory<ColoringRecipeParams, R> factory) {
        return new RecipeSerializer<>(
                ProcessingRecipe.codec(factory, ColoringRecipeParams.CODEC),
                ProcessingRecipe.streamCodec(factory, ColoringRecipeParams.STREAM_CODEC));
    }
}
