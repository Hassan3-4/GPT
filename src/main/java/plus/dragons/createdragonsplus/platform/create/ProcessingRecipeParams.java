/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.platform.create;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;

/** Mutable payload shared by CDP's Create 6.0.10-style processing recipes. */
public class ProcessingRecipeParams {
    protected Identifier id;
    protected final List<Ingredient> ingredients = new ArrayList<>();
    protected final List<ProcessingOutput> results = new ArrayList<>();
    protected final List<FluidIngredient> fluidIngredients = new ArrayList<>();
    protected final List<FluidStack> fluidResults = new ArrayList<>();
    protected int processingDuration;
    protected HeatCondition requiredHeat = HeatCondition.NONE;

    protected ProcessingRecipeParams() {
    }

    protected ProcessingRecipeParams(Identifier id) {
        this.id = id;
    }

    public static <P extends ProcessingRecipeParams> MapCodec<P> codec(Supplier<P> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(params -> params.ingredients),
            ProcessingOutput.CODEC.listOf().optionalFieldOf("results", List.of()).forGetter(params -> params.results),
            FluidIngredient.CODEC.listOf().optionalFieldOf("fluid_ingredients", List.of())
                .forGetter(params -> params.fluidIngredients),
            FluidStack.CODEC.listOf().optionalFieldOf("fluid_results", List.of()).forGetter(params -> params.fluidResults),
            net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("processing_time", 0)
                .forGetter(params -> params.processingDuration),
            HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE)
                .forGetter(params -> params.requiredHeat)
        ).apply(instance, (ingredients, results, fluidIngredients, fluidResults, duration, heat) -> {
            P params = factory.get();
            params.ingredients.addAll(ingredients);
            params.results.addAll(results);
            params.fluidIngredients.addAll(fluidIngredients);
            params.fluidResults.addAll(fluidResults);
            params.processingDuration = duration;
            params.requiredHeat = heat;
            return params;
        }));
    }

    public static <P extends ProcessingRecipeParams> StreamCodec<RegistryFriendlyByteBuf, P> streamCodec(Supplier<P> factory) {
        return new StreamCodec<>() {
            @Override
            public P decode(RegistryFriendlyByteBuf buffer) {
                P params = factory.get();
                params.decode(buffer);
                return params;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, P params) {
                params.encode(buffer);
            }
        };
    }

    protected void encode(RegistryFriendlyByteBuf buffer) {
        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, ingredients);
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, results);
        FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()).encode(buffer, fluidIngredients);
        FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()).encode(buffer, fluidResults);
        ByteBufCodecs.VAR_INT.encode(buffer, processingDuration);
        HeatCondition.PACKET_CODEC.encode(buffer, requiredHeat);
    }

    protected void decode(RegistryFriendlyByteBuf buffer) {
        ingredients.clear();
        ingredients.addAll(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
        results.clear();
        results.addAll(ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
        fluidIngredients.clear();
        fluidIngredients.addAll(FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
        fluidResults.clear();
        fluidResults.addAll(FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()).decode(buffer));
        processingDuration = ByteBufCodecs.VAR_INT.decode(buffer);
        requiredHeat = HeatCondition.PACKET_CODEC.decode(buffer);
    }

    public List<Ingredient> ingredients() {
        return List.copyOf(ingredients);
    }

    public List<ProcessingOutput> results() {
        return List.copyOf(results);
    }

    public List<FluidIngredient> fluidIngredients() {
        return List.copyOf(fluidIngredients);
    }

    public List<FluidStack> fluidResults() {
        return fluidResults.stream().map(FluidStack::copy).toList();
    }

    public int processingDuration() {
        return processingDuration;
    }

    public HeatCondition requiredHeat() {
        return requiredHeat;
    }

    public Ingredient requireSingleItemIngredient() {
        if (ingredients.size() != 1) {
            throw new IllegalStateException("Recipe " + id + " requires exactly one item ingredient");
        }
        return ingredients.getFirst();
    }

    public ProcessingOutput requireSingleItemOutput() {
        if (results.size() != 1) {
            throw new IllegalStateException("Recipe " + id + " requires exactly one item output");
        }
        return results.getFirst();
    }

    public net.minecraft.world.item.ItemStack requireSingleItemResult() {
        return requireSingleItemOutput().create();
    }

    public FluidIngredient requireSingleFluidIngredient() {
        if (fluidIngredients.size() != 1) {
            throw new IllegalStateException("Recipe " + id + " requires exactly one fluid ingredient");
        }
        return fluidIngredients.getFirst();
    }

    public FluidStack requireSingleFluidResult() {
        if (fluidResults.size() != 1) {
            throw new IllegalStateException("Recipe " + id + " requires exactly one fluid output");
        }
        return fluidResults.getFirst().copy();
    }
}
