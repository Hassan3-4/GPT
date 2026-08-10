/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The small recipe-type contract used by Create 6.0.10 processing recipes.
 *
 * <p>Create Fly 6.0.9 uses dedicated record recipes instead of Create's
 * 6.0.10 processing-recipe base classes.  CDP keeps this contract in its own
 * namespace so its custom recipe ids and serializers remain normal Fabric
 * registry entries without modifying Create Fly.</p>
 */
public interface IRecipeTypeInfo {
    Identifier getId();

    RecipeSerializer<? extends Recipe<?>> getSerializer();

    RecipeType<? extends Recipe<?>> getType();
}
