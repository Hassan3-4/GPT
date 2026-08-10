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

package plus.dragons.createdragonsplus.common.fluids.hatch;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.content.fluids.transfer.FillingInput;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import java.util.Optional;
import java.util.OptionalInt;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FluidHatchFillingRecipeTransfer {
    public static boolean canItemBeFilled(Level level, ItemStack stack) {
        return level.recipeAccess().propertySet(AllRecipeSets.FILLING).test(stack);
    }

    public static OptionalInt getRequiredAmountForItem(Level level, ItemStack stack, FluidStack availableFluid) {
        return findRecipe(level, stack, availableFluid)
                .map(RecipeHolder::value)
                .map(FillingRecipe::fluidIngredient)
                .map(ingredient -> ingredient.amount())
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }

    public static Optional<ItemStack> fillItem(Level level, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copyWithAmount(requiredAmount);

        return findRecipe(level, stack, toFill)
                .map(RecipeHolder::value)
                .map(recipe -> {
                    ItemStack result = recipe.assemble(new FillingInput(stack, toFill), level.registryAccess());
                    availableFluid.decrement(requiredAmount);
                    stack.shrink(1);
                    return result;
                });
    }

    private static Optional<RecipeHolder<FillingRecipe>> findRecipe(Level level, ItemStack stack, FluidStack availableFluid) {
        if (!(level instanceof ServerLevel serverLevel))
            return Optional.empty();
        FillingInput input = new FillingInput(stack, availableFluid);
        return serverLevel.recipeAccess().getRecipeFor(AllRecipeTypes.FILLING, input, serverLevel);
    }
}
