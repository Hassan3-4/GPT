/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.integration.jei.category;

import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.widget.ChanceTooltip;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;

public final class CEIJeiHelper {
    private CEIJeiHelper() {}

    public static IDrawable getRenderedSlot() {
        return CreateCategory.SLOT;
    }

    public static IDrawable getRenderedSlot(ProcessingOutput output) {
        return output.chance() == 1 ? CreateCategory.SLOT : CreateCategory.CHANCE_SLOT;
    }

    public static IRecipeSlotRichTooltipCallback addStochasticTooltip(ProcessingOutput output) {
        return new ChanceTooltip(output.chance());
    }

    public static IRecipeSlotBuilder addFluidSlot(
            IRecipeLayoutBuilder builder, int x, int y, FluidIngredient ingredient) {
        return CreateCategory.addFluidSlot(builder, x, y, ingredient);
    }

    public static IRecipeSlotBuilder addFluidSlot(
            IRecipeLayoutBuilder builder, int x, int y, FluidStack stack) {
        return CreateCategory.addFluidSlot(builder, x, y, stack);
    }
}
