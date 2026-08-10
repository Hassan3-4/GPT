/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.integration.jei.category.assembly;

import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory.SequencedRenderer;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.integration.jei.category.CEIJeiHelper;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.AnimatedPrinter;

public class AssemblyPrintingCategory extends SequencedRenderer<PrintingRecipe> {
    private final AnimatedPrinter printer = new AnimatedPrinter();

    @Override
    public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, PrintingRecipe recipe) {
        IRecipeSlotBuilder item = builder.addInputSlot(x - 5, y)
                .setBackground(CreateCategory.SLOT, -1, -1)
                .add(recipe.getIngredients().get(1));
        var fluids = recipe.getFluidIngredients();
        if (!fluids.isEmpty())
            CEIJeiHelper.addFluidSlot(builder, x + 13, y, fluids.getFirst());
        return item;
    }

    @Override
    public void render(
            GuiGraphics graphics, int index, int x, int y, Optional<IRecipeSlotView> slot) {
        printer.offset = index;
        printer.draw(graphics, x - 7, y + 20);
    }
}
