/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.platform.create;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Functional counterpart to Create 6.0.10's processing-recipe base class.
 * It implements Create Fly's rollable-recipe contract, retaining chance and
 * stack-merging behaviour for processing recipes.
 */
public abstract class ProcessingRecipe<T extends RecipeInput, P extends ProcessingRecipeParams>
        implements CreateRollableRecipe<T> {
    protected final IRecipeTypeInfo typeInfo;
    protected final P params;
    protected final List<Ingredient> ingredients;
    protected final List<ProcessingOutput> results;
    protected final List<FluidIngredient> fluidIngredients;
    protected final List<FluidStack> fluidResults;
    protected final int processingDuration;
    protected final HeatCondition requiredHeat;
    private @Nullable Supplier<ItemStack> forcedResult;

    protected ProcessingRecipe(IRecipeTypeInfo typeInfo, P params) {
        this.typeInfo = typeInfo;
        this.params = params;
        this.ingredients = List.copyOf(params.ingredients);
        this.results = List.copyOf(params.results);
        this.fluidIngredients = List.copyOf(params.fluidIngredients);
        this.fluidResults = List.copyOf(params.fluidResults);
        this.processingDuration = params.processingDuration;
        this.requiredHeat = params.requiredHeat;
        validate();
    }

    protected abstract int getMaxInputCount();
    protected abstract int getMaxOutputCount();
    protected boolean canRequireHeat() { return false; }
    protected boolean canSpecifyDuration() { return false; }
    protected int getMaxFluidInputCount() { return 0; }
    protected int getMaxFluidOutputCount() { return 0; }

    private void validate() {
        if (ingredients.size() > getMaxInputCount() || results.size() > getMaxOutputCount()
                || fluidIngredients.size() > getMaxFluidInputCount() || fluidResults.size() > getMaxFluidOutputCount()) {
            throw new IllegalArgumentException("Invalid " + typeInfo.getId() + " processing recipe: too many inputs or outputs");
        }
        if (processingDuration < 0) throw new IllegalArgumentException("Processing duration cannot be negative");
    }

    /** Legacy Create 6-style item ingredient accessor retained for CEI callers. */
    public List<Ingredient> getIngredients() { return ingredients; }
    public List<FluidIngredient> getFluidIngredients() { return fluidIngredients; }
    public List<ProcessingOutput> getRollableResults() { return results; }
    public List<FluidStack> getFluidResults() { return fluidResults; }
    public List<ItemStack> getRollableResultsAsItemStacks() { return results.stream().map(ProcessingOutput::create).toList(); }
    public void enforceNextResult(Supplier<ItemStack> result) { forcedResult = result; }

    public List<ItemStack> rollResults(RandomSource random) { return rollResults(random, results); }

    public List<ItemStack> rollResults(RandomSource random, List<ProcessingOutput> outputs) {
        List<ItemStack> rolled = new ArrayList<>(outputs.size());
        for (int index = 0; index < outputs.size(); index++) {
            ItemStack stack = index == 0 && forcedResult != null ? forcedResult.get() : outputs.get(index).rollOutput(random);
            if (stack != null && !stack.isEmpty()) rolled.add(stack);
        }
        forcedResult = null;
        return rolled;
    }

    public int getProcessingDuration() { return processingDuration; }
    public HeatCondition getRequiredHeat() { return requiredHeat; }
    public IRecipeTypeInfo getTypeInfo() { return typeInfo; }
    public P getParams() { return params; }

    @Override
    public ItemStack assemble(T input, HolderLookup.Provider provider) {
        ItemStack junk = CreateRecipe.getJunk(input.getItem(0));
        return junk != null ? junk : getResultItem(provider).copy();
    }

    @Override
    public List<ItemStack> assemble(T input, RandomSource random) {
        ItemStack junk = CreateRecipe.getJunk(input.getItem(0));
        return junk != null ? List.of(junk) : rollResults(random);
    }

    /** Deterministic preview result used by recipe viewers and item handlers. */
    public ItemStack getResultItem(HolderLookup.Provider provider) { return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().create(); }
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public RecipeSerializer<? extends Recipe<T>> getSerializer() {
        @SuppressWarnings("unchecked")
        RecipeSerializer<? extends Recipe<T>> serializer = (RecipeSerializer<? extends Recipe<T>>) typeInfo.getSerializer();
        return serializer;
    }

    @Override
    public RecipeType<? extends Recipe<T>> getType() {
        @SuppressWarnings("unchecked")
        RecipeType<? extends Recipe<T>> type = (RecipeType<? extends Recipe<T>>) typeInfo.getType();
        return type;
    }

    public static <P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> MapCodec<R> codec(Factory<P, R> factory, MapCodec<P> paramsCodec) {
        return paramsCodec.xmap(factory::create, ProcessingRecipe::getParams);
    }

    public static <P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(Factory<P, R> factory, StreamCodec<RegistryFriendlyByteBuf, P> paramsCodec) {
        return paramsCodec.map(factory::create, ProcessingRecipe::getParams);
    }

    @FunctionalInterface
    public interface Factory<P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> { R create(P params); }
}
