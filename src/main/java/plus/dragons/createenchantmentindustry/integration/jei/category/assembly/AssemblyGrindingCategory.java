/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.integration.jei.category.assembly;

import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory.SequencedRenderer;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.integration.jei.category.CEIJeiHelper;
import plus.dragons.createenchantmentindustry.integration.jei.category.grinding.AnimatedGrindstone;

public class AssemblyGrindingCategory extends SequencedRenderer<GrindingRecipe> {
    private final AnimatedGrindstone grindstone = new AnimatedGrindstone();

    @Override
    public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, GrindingRecipe recipe) {
        var fluids = recipe.getFluidIngredients();
        return fluids.isEmpty() ? null : CEIJeiHelper.addFluidSlot(builder, x + 4, y, fluids.getFirst());
    }

    @Override
    public void render(
            GuiGraphics graphics, int index, int x, int y, Optional<IRecipeSlotView> slot) {
        grindstone.offset = index;
        grindstone.draw(graphics, x - 7, y + 18);
    }
}
