/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

/** Fabric data builder for Create Fly mechanical-crafting recipes. */
public final class CreateFlyMechanicalCraftingRecipeBuilder {
    private final ItemStack result;
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final List<String> pattern = new ArrayList<>();
    private boolean acceptMirrored = true;

    public CreateFlyMechanicalCraftingRecipeBuilder(ItemLike result, int count) {
        this.result = new ItemStack(result, count);
    }

    public CreateFlyMechanicalCraftingRecipeBuilder key(char symbol, ItemLike item) {
        return key(symbol, Ingredient.of(item));
    }

    public CreateFlyMechanicalCraftingRecipeBuilder key(char symbol, TagKey<Item> tag) {
        return key(symbol, Ingredient.of(StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value)));
    }

    public CreateFlyMechanicalCraftingRecipeBuilder key(char symbol, Ingredient ingredient) {
        if (symbol == ' ' || key.putIfAbsent(symbol, ingredient) != null) {
            throw new IllegalArgumentException("Mechanical crafting symbol must be unique and non-whitespace: " + symbol);
        }
        return this;
    }

    public CreateFlyMechanicalCraftingRecipeBuilder patternLine(String line) {
        if (line.isEmpty() || !pattern.isEmpty() && line.length() != pattern.getFirst().length()) {
            throw new IllegalArgumentException("Mechanical crafting pattern lines must be non-empty and equal-width");
        }
        pattern.add(line);
        return this;
    }

    public CreateFlyMechanicalCraftingRecipeBuilder disallowMirrored() {
        acceptMirrored = false;
        return this;
    }

    public MechanicalCraftingRecipe build() {
        if (pattern.isEmpty()) {
            throw new IllegalStateException("Mechanical crafting recipe has no pattern");
        }
        return new MechanicalCraftingRecipe(ShapedRecipePattern.of(key, pattern), result.copy(), acceptMirrored);
    }

    public void build(RecipeOutput output, Identifier id) {
        output.accept(ResourceKey.create(Registries.RECIPE, id), build(), null);
    }

    public void build(RecipeOutput output) {
        Identifier id = result.getItemHolder().unwrapKey().orElseThrow().identifier();
        build(output, id);
    }
}
