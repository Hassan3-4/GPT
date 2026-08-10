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

package plus.dragons.createenchantmentindustry.integration.jei.category;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.ApiStatus.Internal;
import plus.dragons.createdragonsplus.util.ErrorMessages;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.integration.jei.category.grinding.GrindingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyGrindingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyPrintingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.*;

@JeiPlugin
public class CEIJeiPlugin implements IModPlugin {
    public static final Identifier ID = CEICommon.asResource("jei");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        SequencedAssemblyCategory.registerRenderer(
                CEIRecipes.GRINDING.getType(), new AssemblyGrindingCategory());
        SequencedAssemblyCategory.registerRenderer(
                CEIRecipes.PRINTING.getType(), new AssemblyPrintingCategory());
        registration.addRecipeCategories(
                new PrintingCategory(),
                new GrindingCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = getRecipeManager();
        registration.addRecipes(PrintingCategory.TYPE, recipesOf(recipeManager, CEIRecipes.PRINTING.getType())
                .stream()
                .map(StandardPrintingRecipeJEI::new)
                .collect(Collectors.toList()));
        List<PrintingRecipeJEI> builtinPrinting = new ArrayList<>();
        if (CEIConfig.fluids().enablePackageAddressPrinting.get()) builtinPrinting.add(AddressPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enablePackagePatternPrinting.get()) builtinPrinting.add(PatternPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableCreateCopiableItemPrinting.get()) builtinPrinting.add(CopyPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableCustomNamePrinting.get()) builtinPrinting.add(CustomNamePrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableWrittenBookPrinting.get()) builtinPrinting.add(WrittenBookPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableBannerPatternPrinting.get()) builtinPrinting.add(BannerPatternPrintingRecipeJEI.INSTANCE);
        if (!builtinPrinting.isEmpty())
            registration.addRecipes(PrintingCategory.TYPE, builtinPrinting);
        if (CEIConfig.fluids().enableEnchantedBookPrinting.get())
            registration.addRecipes(PrintingCategory.TYPE, EnchantedBookPrintingRecipeJEI.listAll());
        registration.addRecipes(JeiClientPlugin.ITEM_APPLICATION, List.of(MechanicalGrindStoneItem.createRecipe()));
        registration.addRecipes(GrindingCategory.TYPE, recipesOf(recipeManager, CEIRecipes.GRINDING.getType()));
        RecipeType<SandPaperPolishingRecipe> polishing = AllRecipeTypes.SANDPAPER_POLISHING;
        registration.addRecipes(GrindingCategory.TYPE, recipesOf(recipeManager, polishing)
                .stream()
                .map(GrindingRecipe::fromPolishing)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(PrintingCategory.TYPE, CEIBlocks.PRINTER);
        registration.addRecipeCatalysts(GrindingCategory.TYPE, CEIBlocks.MECHANICAL_GRINDSTONE);
    }

    @Internal
    private static <I extends RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> List<RecipeHolder<T>> recipesOf(RecipeMap recipes, RecipeType<T> type) {
        return List.copyOf(recipes.byType(type));
    }

    public static RecipeMap getRecipeManager() {
        return mezz.jei.common.Internal.getClientSyncedRecipes();
    }
}
