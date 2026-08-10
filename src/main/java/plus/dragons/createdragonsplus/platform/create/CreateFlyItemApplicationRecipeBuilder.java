/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Builds Create Fly's concrete deployer/item-application recipe records. */
public final class CreateFlyItemApplicationRecipeBuilder<R extends ItemApplicationRecipe> {
    private final Identifier recipeId;
    private final Factory<R> factory;
    private final List<ProcessingOutput> results = new ArrayList<>();
    private Ingredient target;
    private Ingredient ingredient;
    private boolean keepHeldItem;

    public CreateFlyItemApplicationRecipeBuilder(Factory<R> factory, Identifier recipeId) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.recipeId = Objects.requireNonNull(recipeId, "recipeId");
    }

    /** The first required item is the deployer's target; the second is its held item. */
    public CreateFlyItemApplicationRecipeBuilder<R> require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    /** The first required ingredient is the deployer's target; the second is its held item. */
    public CreateFlyItemApplicationRecipeBuilder<R> require(Ingredient value) {
        if (target == null) {
            target = value;
        } else if (ingredient == null) {
            ingredient = value;
        } else {
            throw new IllegalStateException("Item application recipes accept exactly a target and a held ingredient");
        }
        return this;
    }

    public CreateFlyItemApplicationRecipeBuilder<R> requireTarget(ItemLike item) {
        return requireTarget(Ingredient.of(item));
    }

    public CreateFlyItemApplicationRecipeBuilder<R> requireTarget(Ingredient value) {
        if (target != null) {
            throw new IllegalStateException("Item application target already set");
        }
        target = value;
        return this;
    }

    public CreateFlyItemApplicationRecipeBuilder<R> requireHeld(ItemLike item) {
        return requireHeld(Ingredient.of(item));
    }

    public CreateFlyItemApplicationRecipeBuilder<R> requireHeld(Ingredient value) {
        if (ingredient != null) {
            throw new IllegalStateException("Item application held ingredient already set");
        }
        ingredient = value;
        return this;
    }

    public CreateFlyItemApplicationRecipeBuilder<R> output(ItemLike item) {
        return output(new ItemStack(item));
    }

    public CreateFlyItemApplicationRecipeBuilder<R> output(ItemLike item, int amount) {
        return output(new ItemStack(item, amount));
    }

    public CreateFlyItemApplicationRecipeBuilder<R> output(ItemStack stack) {
        return output(1F, stack);
    }

    public CreateFlyItemApplicationRecipeBuilder<R> output(float chance, ItemStack stack) {
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Output chance must be between 0 and 1");
        }
        results.add(new ProcessingOutput(stack.typeHolder(), stack.getCount(), stack.getComponentsPatch(), chance));
        return this;
    }

    public CreateFlyItemApplicationRecipeBuilder<R> output(ProcessingOutput output) {
        results.add(Objects.requireNonNull(output, "output"));
        return this;
    }

    public CreateFlyItemApplicationRecipeBuilder<R> keepHeldItem() {
        keepHeldItem = true;
        return this;
    }

    public R build() {
        if (target == null || ingredient == null || results.isEmpty()) {
            throw new IllegalStateException("Item application recipes require target, held ingredient and output");
        }
        return factory.create(List.copyOf(results), keepHeldItem, target, ingredient);
    }

    public void build(RecipeOutput output) {
        output.accept(ResourceKey.create(Registries.RECIPE, recipeId), build(), null);
    }

    @FunctionalInterface
    public interface Factory<R extends ItemApplicationRecipe> {
        R create(List<ProcessingOutput> results, boolean keepHeldItem, Ingredient target, Ingredient ingredient);
    }
}
