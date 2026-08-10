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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import com.zurrtum.create.client.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingInput;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class RecipePrintingBehaviour implements PrintingBehaviour {
    public static final RecipePrintingBehaviour EMPTY = new RecipePrintingBehaviour(ItemStack.EMPTY);
    private final ItemStack template;
    private @Nullable RecipeHolder<PrintingRecipe> lastRecipe;

    public RecipePrintingBehaviour(ItemStack template) {
        this.template = template;
    }

    private Optional<PrintingRecipe> findRecipe(Level level, ItemStack stack, FluidStack fluidStack) {
        var input = new PrintingInput(stack, template, fluidStack);
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.ofNullable(lastRecipe).map(RecipeHolder::value);
        }
        var holder = serverLevel.recipeAccess().getRecipeFor(CEIRecipes.PRINTING.getType(), input, level, lastRecipe);
        if (holder.isPresent()) {
            lastRecipe = holder.get();
            return holder.map(RecipeHolder::value);
        }
        lastRecipe = null;
        return Optional.empty();
    }

    @Override
    public boolean isValid() {
        return !template.isEmpty();
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        return findRecipe(level, stack, FluidStack.EMPTY).isPresent() ? 1 : 0;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        return findRecipe(level, stack, fluidStack)
                .map(recipe -> recipe.getFluidIngredients().getFirst().amount())
                .orElse(0);
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        return findRecipe(level, stack, fluidStack)
                .map(recipe -> recipe.getRollableResults().getFirst().create())
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        if (lastRecipe != null)
            lastRecipe.value().playSound(level, pos.below(), SoundSource.BLOCKS);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (template.isEmpty())
            return false;
        CEILang.translate("gui.goggles.printing.template").forGoggles(tooltip);
        CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        if (lastRecipe != null) {
            var cost = lastRecipe.value().getFluidIngredients().getFirst().amount();
            CEILang.translate("gui.goggles.printing.cost",
                    CEILang.number(FluidUnits.toMillibuckets(cost))
                            .add(CreateLang.translate("generic.unit.millibuckets"))
                            .style(cost <= FluidUnits.fromMillibuckets(CEIConfig.fluids().printerFluidCapacity.get())
                                    ? ChatFormatting.GREEN
                                    : ChatFormatting.RED))
                    .forGoggles(tooltip, 1);
        }
        return true;
    }
}
