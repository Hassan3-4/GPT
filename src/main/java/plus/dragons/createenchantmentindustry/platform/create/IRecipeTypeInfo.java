/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.platform.create;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Recipe type information for CEI's Create 6.0.10-style processing recipes.
 *
 * <p>Create Fly 6.0.9 represents its recipes as dedicated records. CEI keeps
 * this small contract in its own namespace so custom recipe types remain
 * ordinary Fabric registry entries without changing Create Fly.</p>
 */
public interface IRecipeTypeInfo {
    Identifier getId();

    RecipeSerializer<? extends Recipe<?>> getSerializer();

    RecipeType<? extends Recipe<?>> getType();
}
