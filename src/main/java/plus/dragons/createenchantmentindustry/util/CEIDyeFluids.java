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

package plus.dragons.createenchantmentindustry.util;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class CEIDyeFluids {
    public static Fluid get(DyeColor color) {
        return BuiltInRegistries.FLUID.getValue(fluidId(color));
    }

    public static ItemStack bucket(DyeColor color) {
        return BuiltInRegistries.ITEM.getValue(fluidId(color).withSuffix("_bucket")).getDefaultInstance();
    }

    public static TagKey<Fluid> tag(DyeColor color) {
        return TagKey.create(net.minecraft.core.registries.Registries.FLUID,
                Identifier.fromNamespaceAndPath("c", "dyes/" + color.getName()));
    }

    public static DyeColor color(Fluid fluid) {
        String path = BuiltInRegistries.FLUID.getKey(fluid).getPath();
        for (DyeColor color : DyeColor.values()) {
            if (path.equals(color.getName() + "_dye"))
                return color;
        }
        return DyeColor.BLACK;
    }

    private static Identifier fluidId(DyeColor color) {
        return Identifier.fromNamespaceAndPath("create_dragons_plus", color.getName() + "_dye");
    }
}
