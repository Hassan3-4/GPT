/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.foundation.fluid.FluidTagIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Objects;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

/** Fluent builder used for CDP's Create 6.0.10-compatible processing recipes. */
public abstract class ProcessingRecipeBuilder<P extends ProcessingRecipeParams,
        R extends ProcessingRecipe<?, P>, B extends ProcessingRecipeBuilder<P, R, B>> {
    protected final ProcessingRecipe.Factory<P, R> factory;
    protected final Identifier recipeId;
    protected final P params;

    protected ProcessingRecipeBuilder(ProcessingRecipe.Factory<P, R> factory, Identifier recipeId) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.recipeId = Objects.requireNonNull(recipeId, "recipeId");
        this.params = createParams();
        this.params.id = recipeId;
    }

    protected abstract P createParams();

    protected abstract B self();

    public B require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public B require(TagKey<net.minecraft.world.item.Item> tag) {
        var holders = BuiltInRegistries.ITEM.getTags()
                .filter(set -> set.key().equals(tag))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unbound item tag " + tag.location()));
        return require(Ingredient.of(holders));
    }

    public B require(Ingredient ingredient) {
        params.ingredients.add(Objects.requireNonNull(ingredient, "ingredient"));
        return self();
    }

    public B require(Fluid fluid, int amount) {
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        return require(new FluidStackIngredient(source, net.minecraft.core.component.DataComponentPatch.EMPTY, amount));
    }

    public B require(TagKey<Fluid> tag, int amount) {
        return require(new FluidTagIngredient(tag, amount));
    }

    public B require(FluidIngredient ingredient) {
        params.fluidIngredients.add(Objects.requireNonNull(ingredient, "ingredient"));
        return self();
    }

    public B output(ItemLike item) {
        return output(item, 1);
    }

    public B output(ItemLike item, int amount) {
        return output(1F, item, amount);
    }

    public B output(float chance, ItemLike item) {
        return output(chance, item, 1);
    }

    public B output(float chance, ItemLike item, int amount) {
        return output(chance, new ItemStack(item, amount));
    }

    public B output(ItemStack stack) {
        return output(1F, stack);
    }

    public B output(float chance, ItemStack stack) {
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Output chance must be between 0 and 1");
        }
        params.results.add(new ProcessingOutput(stack.getItemHolder(), stack.getCount(), stack.getComponentsPatch(), chance));
        return self();
    }

    public B output(ProcessingOutput output) {
        params.results.add(Objects.requireNonNull(output, "output"));
        return self();
    }

    public B output(Fluid fluid, int amount) {
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        return output(new FluidStack(source, amount));
    }

    public B output(FluidStack output) {
        params.fluidResults.add(Objects.requireNonNull(output, "output").copy());
        return self();
    }

    public B duration(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Processing duration cannot be negative");
        }
        params.processingDuration = ticks;
        return self();
    }

    public B averageProcessingDuration() {
        return duration(100);
    }

    public B requiresHeat(HeatCondition condition) {
        params.requiredHeat = Objects.requireNonNull(condition, "condition");
        return self();
    }

    public R build() {
        return factory.create(params);
    }

    public void build(RecipeOutput output) {
        output.accept(ResourceKey.create(Registries.RECIPE, recipeId), build(), null);
    }
}
