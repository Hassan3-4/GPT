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
import java.util.function.Function;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

/**
 * Converts the 6.0.10 builder syntax used by CDP's data providers to the
 * concrete recipe records exposed by Create Fly 6.0.9.
 */
public final class CreateFlyProcessingRecipeBuilder<R extends Recipe<?>> {
    private final Identifier recipeId;
    private final ProcessingRecipeParams params;
    private final Function<ProcessingRecipeParams, R> factory;
    private ResourceCondition loadCondition;

    public CreateFlyProcessingRecipeBuilder(Function<ProcessingRecipeParams, R> factory, Identifier recipeId) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.recipeId = Objects.requireNonNull(recipeId, "recipeId");
        this.params = new ProcessingRecipeParams(recipeId);
    }

    public CreateFlyProcessingRecipeBuilder<R> require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public CreateFlyProcessingRecipeBuilder<R> require(TagKey<net.minecraft.world.item.Item> tag) {
        // Datagen has no baked tag contents. An empty named holder set keeps
        // the tag reference intact so RecipeOutput serializes it through
        // RegistryOps, without resolving it to an empty item list.
        return require(Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
    }

    public CreateFlyProcessingRecipeBuilder<R> require(Ingredient ingredient) {
        params.ingredients.add(Objects.requireNonNull(ingredient, "ingredient"));
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> require(Fluid fluid, int amount) {
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        return require(new FluidStackIngredient(source, net.minecraft.core.component.DataComponentPatch.EMPTY, amount));
    }

    public CreateFlyProcessingRecipeBuilder<R> require(TagKey<Fluid> tag, int amount) {
        return require(new FluidTagIngredient(tag, amount));
    }

    public CreateFlyProcessingRecipeBuilder<R> require(FluidIngredient ingredient) {
        params.fluidIngredients.add(Objects.requireNonNull(ingredient, "ingredient"));
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> output(ItemLike item) {
        return output(item, 1);
    }

    public CreateFlyProcessingRecipeBuilder<R> output(ItemLike item, int amount) {
        return output(1F, item, amount);
    }

    public CreateFlyProcessingRecipeBuilder<R> output(float chance, ItemLike item, int amount) {
        return output(chance, new ItemStack(item, amount));
    }

    public CreateFlyProcessingRecipeBuilder<R> output(ItemStack stack) {
        return output(1F, stack);
    }

    public CreateFlyProcessingRecipeBuilder<R> output(float chance, ItemStack stack) {
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Output chance must be between 0 and 1");
        }
        params.results.add(new ProcessingOutput(stack.getItemHolder(), stack.getCount(), stack.getComponentsPatch(), chance));
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> output(ProcessingOutput output) {
        params.results.add(Objects.requireNonNull(output, "output"));
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> output(Fluid fluid, int amount) {
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        return output(new FluidStack(source, amount));
    }

    public CreateFlyProcessingRecipeBuilder<R> output(FluidStack output) {
        params.fluidResults.add(Objects.requireNonNull(output, "output").copy());
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> duration(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Processing duration cannot be negative");
        }
        params.processingDuration = ticks;
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> averageProcessingDuration() {
        return duration(100);
    }

    public CreateFlyProcessingRecipeBuilder<R> requiresHeat(HeatCondition condition) {
        params.requiredHeat = Objects.requireNonNull(condition, "condition");
        return this;
    }

    public CreateFlyProcessingRecipeBuilder<R> whenModLoaded(String modId) {
        loadCondition = ResourceConditions.allModsLoaded(modId);
        return this;
    }

    public R build() {
        return factory.apply(params);
    }

    public void build(RecipeOutput output) {
        R recipe = build();
        if (loadCondition != null) FabricDataGenHelper.addConditions(recipe, new ResourceCondition[] {loadCondition});
        output.accept(ResourceKey.create(Registries.RECIPE, recipeId), recipe, null);
    }
}
