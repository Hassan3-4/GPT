/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.mixin.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidTagIngredient;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import java.util.List;
import java.util.SortedMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.config.CDPConfig;

/**
 * Adds the fluid-dragon-breath conversion recipes after Create Fly has
 * materialised its dynamic vanilla potion recipes.  Create 6.0.9 replaced
 * PotionMixingRecipes with PotionRecipe, so hooking the old class would both
 * fail to load and silently remove this CDP feature.
 */
@Mixin(value = RecipeManager.class, priority = 900)
public class PotionMixingRecipesMixin {
    @Inject(
        method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;size()I", shift = At.Shift.AFTER)
    )
    private void cdp$addDragonBreathFluidRecipes(
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfoReturnable<?> cir,
            @Local SortedMap<Identifier, Recipe<?>> sortedMap) {
        if (!CDPConfig.features().generateAutomaticBrewingRecipeForDragonBreathFluid.get()) {
            return;
        }

        List<MixingRecipe> recipes = sortedMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof PotionRecipe)
            .map(entry -> createRecipe(entry.getKey(), (PotionRecipe) entry.getValue()))
            .flatMap(java.util.Optional::stream)
            .toList();

        for (int index = 0; index < recipes.size(); index++) {
            MixingRecipe recipe = recipes.get(index);
            Identifier id = CDPCommon.asResource("mixing/dragon_breath_potion_" + index);
            sortedMap.put(id, recipe);
        }
    }

    private static java.util.Optional<MixingRecipe> createRecipe(Identifier id, PotionRecipe potion) {
        if (!potion.ingredient().test(new ItemStack(Items.DRAGON_BREATH))) {
            return java.util.Optional.empty();
        }
        BottleType from = potion.fluidIngredient().getMatchingFluidStacks().stream()
            .findFirst()
            .map(stack -> stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR))
            .orElse(BottleType.REGULAR);
        BottleType to = potion.result().getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
        if (from != BottleType.SPLASH || to != BottleType.LINGERING) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new MixingRecipe(
            0,
            List.of(),
            List.of(potion.result().copy()),
            HeatCondition.HEATED,
            List.of(new FluidTagIngredient(CDPFluids.COMMON_TAGS.dragonBreath, 250), potion.fluidIngredient()),
            List.<SizedIngredient>of()
        ));
    }
}
