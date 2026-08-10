/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

/** Builds raw Create Fly sequenced-assembly recipes from already-built step recipes. */
public final class CreateFlySequencedAssemblyRecipeBuilder {
    private final Identifier id;
    private Ingredient ingredient;
    private ItemStack transitionalItem;
    private ProcessingOutput result;
    private final List<ProcessingOutput> junks = new ArrayList<>();
    private final List<Recipe<?>> sequence = new ArrayList<>();
    private int loops = 1;

    public CreateFlySequencedAssemblyRecipeBuilder(Identifier id) {
        this.id = id;
    }

    public CreateFlySequencedAssemblyRecipeBuilder require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public CreateFlySequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
        return require(Ingredient.of(StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value)));
    }

    public CreateFlySequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public CreateFlySequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
        transitionalItem = new ItemStack(item);
        return this;
    }

    public CreateFlySequencedAssemblyRecipeBuilder loops(int loops) {
        if (loops < 1) {
            throw new IllegalArgumentException("Sequenced assembly must loop at least once");
        }
        this.loops = loops;
        return this;
    }

    public CreateFlySequencedAssemblyRecipeBuilder addOutput(ItemLike item, float chance) {
        return addOutput(new ItemStack(item), chance);
    }

    public CreateFlySequencedAssemblyRecipeBuilder addOutput(ItemStack item, float chance) {
        ProcessingOutput output = new ProcessingOutput(item.typeHolder(), item.getCount(), item.getComponentsPatch(), chance);
        if (result == null) {
            result = output;
        } else {
            junks.add(output);
        }
        return this;
    }

    public CreateFlySequencedAssemblyRecipeBuilder addStep(Recipe<?> step) {
        sequence.add(step);
        return this;
    }

    public SequencedAssemblyRecipe build() {
        if (ingredient == null || transitionalItem == null || result == null || sequence.isEmpty()) {
            throw new IllegalStateException("Sequenced assembly requires ingredient, transitional item, output and at least one step");
        }
        return new SequencedAssemblyRecipe(ingredient, transitionalItem.copy(), result, List.copyOf(junks), loops, List.copyOf(sequence));
    }

    public void build(RecipeOutput output) {
        output.accept(ResourceKey.create(Registries.RECIPE, id), build(), null);
    }
}
