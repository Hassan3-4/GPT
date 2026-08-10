/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import plus.dragons.createdragonsplus.platform.create.StandardProcessingRecipe;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;
import plus.dragons.createdragonsplus.common.recipe.RecipeTypeInfo;

public class CDPRecipes {
    public static final RecipeTypeInfo<ColoringRecipe> COLORING = register("coloring", () -> ColoringRecipe.serializer(ColoringRecipe::new));
    public static final RecipeTypeInfo<FreezingRecipe> FREEZING = register("freezing", () -> StandardProcessingRecipe.serializer(FreezingRecipe::new));
    public static final RecipeTypeInfo<SandingRecipe> SANDING = register("sanding", () -> StandardProcessingRecipe.serializer(SandingRecipe::new));
    public static final RecipeTypeInfo<EndingRecipe> ENDING = register("ending", () -> StandardProcessingRecipe.serializer(EndingRecipe::new));

    public static void register() {
        // Static initialisation performs Fabric registry registration.
    }

    private static <R extends Recipe<?>> RecipeTypeInfo<R> register(String name, Supplier<? extends RecipeSerializer<R>> serializer) {
        return new RecipeTypeInfo<>(name, serializer);
    }
}
