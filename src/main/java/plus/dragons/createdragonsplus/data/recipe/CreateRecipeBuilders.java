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

package plus.dragons.createdragonsplus.data.recipe;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import plus.dragons.createdragonsplus.platform.create.CreateFlyProcessingRecipeBuilder;
import plus.dragons.createdragonsplus.platform.create.CreateFlyItemApplicationRecipeBuilder;
import plus.dragons.createdragonsplus.platform.create.CreateFlyMechanicalCraftingRecipeBuilder;
import plus.dragons.createdragonsplus.platform.create.CreateFlySequencedAssemblyRecipeBuilder;

public class CreateRecipeBuilders {
    public static CreateFlyProcessingRecipeBuilder<CrushingRecipe> crushing(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new CrushingRecipe(
                params.processingDuration(), params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<CuttingRecipe> cutting(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new CuttingRecipe(
                params.processingDuration(), params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<MillingRecipe> milling(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new MillingRecipe(
                params.processingDuration(), params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<MixingRecipe> mixing(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new MixingRecipe(
                params.processingDuration(), params.results(), params.fluidResults(), params.requiredHeat(),
                params.fluidIngredients(), com.zurrtum.create.content.processing.recipe.SizedIngredient.of(params.ingredients())), id);
    }

    public static CreateFlyProcessingRecipeBuilder<CompactingRecipe> compacting(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new CompactingRecipe(
                params.results(), params.requiredHeat(), params.fluidIngredients(),
                com.zurrtum.create.content.processing.recipe.SizedIngredient.of(params.ingredients())), id);
    }

    public static CreateFlyProcessingRecipeBuilder<PressingRecipe> pressing(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new PressingRecipe(
                params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<SandPaperPolishingRecipe> polishing(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new SandPaperPolishingRecipe(
                params.requireSingleItemResult(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<SplashingRecipe> splashing(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new SplashingRecipe(
                params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<HauntingRecipe> haunting(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new HauntingRecipe(
                params.results(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyItemApplicationRecipeBuilder<DeployerApplicationRecipe> deploying(Identifier id) {
        return new CreateFlyItemApplicationRecipeBuilder<>(DeployerApplicationRecipe::new, id);
    }

    public static CreateFlyProcessingRecipeBuilder<FillingRecipe> filling(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new FillingRecipe(
                params.requireSingleItemResult(), params.requireSingleItemIngredient(), params.requireSingleFluidIngredient()), id);
    }

    public static CreateFlyProcessingRecipeBuilder<EmptyingRecipe> emptying(Identifier id) {
        return new CreateFlyProcessingRecipeBuilder<>(params -> new EmptyingRecipe(
                params.requireSingleItemResult(), params.requireSingleFluidResult(), params.requireSingleItemIngredient()), id);
    }

    public static CreateFlyItemApplicationRecipeBuilder<ManualApplicationRecipe> manualApplication(Identifier id) {
        return new CreateFlyItemApplicationRecipeBuilder<>(ManualApplicationRecipe::new, id);
    }

    public static CreateFlyMechanicalCraftingRecipeBuilder mechanicalCrafting(ItemLike item, int count) {
        return new CreateFlyMechanicalCraftingRecipeBuilder(item, count);
    }

    public static CreateFlyMechanicalCraftingRecipeBuilder mechanicalCrafting(ItemLike item) {
        return new CreateFlyMechanicalCraftingRecipeBuilder(item, 1);
    }

    public static CreateFlySequencedAssemblyRecipeBuilder sequencedAssembly(Identifier id) {
        return new CreateFlySequencedAssemblyRecipeBuilder(id);
    }
}
