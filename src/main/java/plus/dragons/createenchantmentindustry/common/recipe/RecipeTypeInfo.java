/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.recipe;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.platform.create.IRecipeTypeInfo;

/** Registers one CEI recipe type and its serializer in Fabric's vanilla registries. */
public class RecipeTypeInfo<R extends Recipe<?>> implements IRecipeTypeInfo {
    private final Identifier id;
    private final RecipeSerializer<R> serializer;
    private final RecipeType<R> type;

    public RecipeTypeInfo(String name, Supplier<? extends RecipeSerializer<R>> serializerFactory) {
        id = CEICommon.asResource(name);
        type = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
        serializer = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerFactory.get());
    }

    @Override public Identifier getId() { return id; }
    @Override public RecipeSerializer<R> getSerializer() { return serializer; }
    @Override public RecipeType<R> getType() { return type; }
}
