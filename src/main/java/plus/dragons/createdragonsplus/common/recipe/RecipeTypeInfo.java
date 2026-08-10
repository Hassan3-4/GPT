/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.common.recipe;

import plus.dragons.createdragonsplus.platform.create.IRecipeTypeInfo;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createdragonsplus.common.CDPCommon;

@SuppressWarnings("unchecked")
public class RecipeTypeInfo<R extends Recipe<?>> implements IRecipeTypeInfo {
    private final Identifier id;
    private final RecipeSerializer<R> serializer;
    private final RecipeType<R> type;

    public RecipeTypeInfo(String name, Supplier<? extends RecipeSerializer<R>> serializerFactory) {
        id = CDPCommon.asResource(name);
        type = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<R>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
        serializer = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerFactory.get());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<R> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<R> getType() {
        return type;
    }
}
