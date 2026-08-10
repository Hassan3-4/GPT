/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.data;

import static com.zurrtum.create.AllBlocks.*;
import static com.zurrtum.create.AllItems.*;
import static net.minecraft.world.item.Items.*;
import static net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.EGGS;
import static plus.dragons.createdragonsplus.common.registry.CDPBlocks.FLUID_HATCH;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE;
import static plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders.manualApplication;
import static plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders.pressing;
import static plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIFluids.EXPERIENCE;
import static plus.dragons.createenchantmentindustry.common.registry.CEIItems.*;


import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup.Provider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIResourceConditions;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import com.zurrtum.create.AllBlocks;

public class CEIRecipeProvider extends net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider {
    private Provider registries;
    private static final String ANDESITE = "andesite";
    private static final String COPPER = "copper";
    private static final String BRASS = "brass";
    private static final String TRAIN = "train";

    public CEIRecipeProvider(FabricDataOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "Create Enchantment Industry Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(Provider registries, RecipeOutput output) {
        this.registries = registries;
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                CEIRecipeProvider.this.generateRecipes(output);
            }
        };
    }

    private void generateRecipes(RecipeOutput output) {
        buildMachineRecipes(output);
        buildMaterialRecipes(output);
        buildExperienceRecipes(output);
    }

    private void buildMachineRecipes(RecipeOutput output) {
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(itemLookup(), RecipeCategory.MISC, MECHANICAL_GRINDSTONE.get().asItem())
                .define('a', ANDESITE_ALLOY)
                .define('s', AllBlocks.SHAFT)
                .pattern("aaa")
                .pattern("asa")
                .pattern("aaa")
                .unlockedBy(ANDESITE, has(ANDESITE_ALLOY))
                .save(output);
        manualApplication(EXPERIENCE_HATCH.getId())
                .require(FLUID_HATCH)
                .require(AllBlocks.EXPERIENCE_BLOCK)
                .output(EXPERIENCE_HATCH.get())
                .build(output);
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(itemLookup(), RecipeCategory.MISC, PRINTER.get().asItem())
                .define('-', net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        Identifier.fromNamespaceAndPath("c", "plates/brass")))
                .define('o', AllBlocks.SPOUT)
                .define('=', net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        Identifier.fromNamespaceAndPath("c", "storage_blocks/iron")))
                .pattern("-")
                .pattern("o")
                .pattern("=")
                .unlockedBy(BRASS, has(BRASS_INGOT))
                .save(output);
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(itemLookup(), RecipeCategory.MISC, EXPERIENCE_LANTERN.get().asItem())
                .define('a', AllBlocks.EXPERIENCE_BLOCK)
                .define('s', SPONGE)
                .define('c', AllBlocks.COPPER_CASING)
                .pattern("a")
                .pattern("s")
                .pattern("c")
                .unlockedBy(COPPER, has(AllBlocks.COPPER_CASING))
                .save(output);
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(AllBlocks.BLAZE_BURNER),
                Ingredient.of(ENCHANTING_TABLE),
                RecipeCategory.MISC,
                BLAZE_ENCHANTER.asItem())
                .unlocks("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                .save(output, BLAZE_ENCHANTER.getId().withPrefix("smithing/").toString());
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(AllBlocks.BLAZE_BURNER),
                Ingredient.of(ANVIL),
                RecipeCategory.MISC,
                BLAZE_FORGER.asItem())
                .unlocks("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                .save(output, BLAZE_FORGER.getId().withPrefix("smithing/").toString());
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(AllBlocks.BLAZE_BURNER),
                Ingredient.of(BLAZES_ENCHANTING_HANDBOOK),
                RecipeCategory.MISC,
                CLASSIC_BLAZE_ENCHANTER.asItem())
                .unlocks("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                .save(withConditions(output, CEIResourceConditions.CLASSIC_BLAZE_ENCHANTER_ENABLED),
                        CLASSIC_BLAZE_ENCHANTER.getId().withPrefix("smithing/").toString());
    }

    private void buildMaterialRecipes(RecipeOutput output) {
        net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless(itemLookup(), RecipeCategory.MISC, SUPER_EXPERIENCE_NUGGET.get(), 9)
                .requires(SUPER_EXPERIENCE_BLOCK.get().asItem())
                .unlockedBy("has_super_experience_block", has(SUPER_EXPERIENCE_BLOCK.get().asItem()))
                .save(output);
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(itemLookup(), RecipeCategory.MISC, SUPER_EXPERIENCE_BLOCK.get().asItem())
                .define('n', SUPER_EXPERIENCE_NUGGET)
                .pattern("nnn")
                .pattern("nnn")
                .pattern("nnn")
                .unlockedBy("has_super_experience_nugget", has(SUPER_EXPERIENCE_NUGGET.get()))
                .save(output);
        pressing(ENCHANTING_TEMPLATE.getId())
                .require(AllBlocks.EXPERIENCE_BLOCK)
                .output(ENCHANTING_TEMPLATE.get())
                .build(output);
        pressing(SUPER_ENCHANTING_TEMPLATE.getId())
                .require(SUPER_EXPERIENCE_BLOCK)
                .output(SUPER_ENCHANTING_TEMPLATE.get())
                .build(output);
        net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless(itemLookup(), RecipeCategory.MISC, BLAZES_ENCHANTING_HANDBOOK.get())
                .requires(BLAZE_UPGRADE_SMITHING_TEMPLATE)
                .requires(STURDY_SHEET)
                .requires(STURDY_SHEET)
                .requires(EXPERIENCE_BOTTLE)
                .requires(EXPERIENCE_BOTTLE)
                .requires(MAGMA_BLOCK)
                .unlockedBy("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                .save(withConditions(output, CEIResourceConditions.CLASSIC_BLAZE_ENCHANTER_ENABLED));
        compacting(EXPERIENCE_CAKE_BASE.getId())
                .require(EGGS)
                .require(SUGAR)
                .require(LAPIS_LAZULI)
                .output(EXPERIENCE_CAKE_BASE.get())
                .build(output);
        filling(EXPERIENCE_CAKE.getId())
                .require(EXPERIENCE_CAKE_BASE)
                .require(EXPERIENCE, FluidUnits.fromMillibuckets(1000))
                .output(EXPERIENCE_CAKE.get())
                .build(output);
        cutting(EXPERIENCE_CAKE_SLICE.getId())
                .require(EXPERIENCE_CAKE)
                .output(EXPERIENCE_CAKE_SLICE.get(), 4)
                .build(output);
    }

    private void buildExperienceRecipes(RecipeOutput output) {
        compacting(CEICommon.asResource("compacting/experience_block"))
                .require(EXPERIENCE, FluidUnits.fromMillibuckets(27))
                .output(AllBlocks.EXPERIENCE_BLOCK)
                .build(output);
        filling(CEICommon.asResource("filling/experience_bottle"))
                .require(EXPERIENCE, FluidUnits.fromMillibuckets(10))
                .require(GLASS_BOTTLE)
                .output(EXPERIENCE_BOTTLE)
                .build(output);
        emptying(CEICommon.asResource("emptying/experience_bottle"))
                .require(EXPERIENCE_BOTTLE)
                .output(EXPERIENCE, FluidUnits.fromMillibuckets(10))
                .output(GLASS_BOTTLE)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("grinding/experience_nugget"))
                .require(EXP_NUGGET)
                .output(EXPERIENCE, FluidUnits.fromMillibuckets(3))
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("grinding/experience_block"))
                .require(AllBlocks.EXPERIENCE_BLOCK)
                .output(EXPERIENCE, FluidUnits.fromMillibuckets(27))
                .build(output);
        crushing(CEICommon.asResource("infested_cobblestone"))
                .require(Blocks.INFESTED_COBBLESTONE)
                .output(Blocks.GRAVEL)
                .output(0.5f, new ItemStack(EXP_NUGGET))
                .build(output);
        compacting(CEICommon.asResource("infested_stone"))
                .require(Blocks.INFESTED_STONE).require(Blocks.INFESTED_STONE)
                .require(Blocks.INFESTED_STONE).require(Blocks.INFESTED_STONE)
                .output(Blocks.STONE_BRICKS)
                .output(new ItemStack(EXP_NUGGET))
                .build(output);
        GrindingRecipe.builder(SUPER_EXPERIENCE_NUGGET.getId().withPrefix("grinding/"))
                .require(SUPER_EXPERIENCE_NUGGET)
                .output(EXPERIENCE, FluidUnits.fromMillibuckets(3))
                .build(output);
        GrindingRecipe.builder(SUPER_EXPERIENCE_BLOCK.getId().withPrefix("grinding/"))
                .require(SUPER_EXPERIENCE_BLOCK)
                .output(EXPERIENCE, FluidUnits.fromMillibuckets(27))
                .build(output);
    }

    private net.minecraft.core.HolderLookup.RegistryLookup<Item> itemLookup() {
        return registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }
}
