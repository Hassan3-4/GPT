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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.platform.create.ProcessingRecipeParams;
import plus.dragons.createenchantmentindustry.platform.create.StandardProcessingRecipe;

public class GrindingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    public GrindingRecipe(ProcessingRecipeParams params) {
        super(CEIRecipes.GRINDING, params);
        if (fluidIngredients.size() + fluidResults.size() > 1)
            throw new IllegalArgumentException("Grinding recipe can only have either 1 fluid input or 1 fluid result");
    }

    public static StandardProcessingRecipe.Builder<GrindingRecipe> builder(Identifier id) {
        return new StandardProcessingRecipe.Builder<>(GrindingRecipe::new, id);
    }

    public static Optional<RecipeHolder<GrindingRecipe>> fromPolishing(RecipeHolder<SandPaperPolishingRecipe> recipe) {
        if (AllRecipeTypes.CAN_BE_AUTOMATED.test(recipe)) {
            var id = recipe.id().identifier().withSuffix("_using_grindstone");
            var polishing = recipe.value();
            var grinding = builder(id)
                    .require(polishing.ingredient())
                    .output(polishing.result())
                    .build();
            return Optional.of(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), grinding));
        }
        return Optional.empty();
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredients.getFirst().test(input.item());
    }

}
