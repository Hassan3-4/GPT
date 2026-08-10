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

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import static plus.dragons.createdragonsplus.common.CDPCommon.PERSISTENT_DATA_KEY;

import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariant;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.CDPIntegrationContributions;
import plus.dragons.createdragonsplus.util.ItemStackKey;

public class ColoringFanProcessingType implements FanProcessingType {
    private static final int CONTACT_COLORING_COOLDOWN = 10;
    private final DyeVariant variant;
    private final Vector3f rgb;
    private final Map<ItemStackKey, Boolean> canProcessCache = new ConcurrentHashMap<>();
    private final Map<ItemStackKey, ItemStack> craftingResultCache = new ConcurrentHashMap<>();
    private final Map<Block, Block> blockColoringResultCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> contactColoringTicks = new ConcurrentHashMap<>();
    private final Map<UUID, ProcessingTimer> entityProcessing = new ConcurrentHashMap<>();
    private static final Identifier SUPPLEMENTARIES_SUS_CRAFTING = Identifier
            .fromNamespaceAndPath("supplementaries", "sus_crafting");

    public ColoringFanProcessingType(DyeVariant variant) {
        this.variant = variant;
        this.rgb = new Color(this.variant.color()).asVectorF();
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.recipes().enableBulkColoring.get())
            return false;
        var dye = CDPFluids.DYES_BY_VARIANT.get(this.variant.id());
        return dye != null && level.getFluidState(pos).getType() == dye.source();
    }

    public void recreateCache() {
        canProcessCache.clear();
        craftingResultCache.clear();
        blockColoringResultCache.clear();
    }

    @Override
    public int getPriority() {
        return 500; // Should be greater than splashing (400)
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.recipes().enableBulkColoring.get())
            return false;
        return canProcessCache.computeIfAbsent(ItemStackKey.of(stack), key -> canProcessUncached(stack, level));
    }

    private boolean canProcessUncached(ItemStack stack, Level level) {
        if (!(level instanceof ServerLevel serverLevel))
            return false;
        var recipe = serverLevel.recipeAccess()
                .getRecipeFor(CDPRecipes.COLORING.getType(), new ColoringRecipeInput(this.variant.id(), stack), level);
        if (recipe.isPresent())
            return true;
        if (CDPIntegrationContributions.canColorByCompat(this.variant, stack, level))
            return true;
        return this.processByCrafting(stack, level).isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        if (!(level instanceof ServerLevel serverLevel))
            return null;
        return serverLevel.recipeAccess()
                .getRecipeFor(CDPRecipes.COLORING.getType(), new ColoringRecipeInput(this.variant.id(), stack), level)
                .map(recipe -> RecipeApplier.applyRecipeOn(level.getRandom(), stack.getCount(),
                        new ColoringRecipeInput(this.variant.id(), stack), recipe.value()))
                .or(() -> CDPIntegrationContributions.processColoringByCompat(this.variant, stack, level))
                .or(() -> processByCrafting(stack, level)
                        .map(result -> ItemHelper.multipliedOutput(result, stack.getCount())))
                .orElse(null);
    }

    public Optional<BlockState> processBlockState(BlockState state, Level level) {
        var block = state.getBlock();
        if (block.asItem() == Items.AIR)
            return Optional.empty();
        var result = blockColoringResultCache.computeIfAbsent(block, key -> processBlockUncached(key, level));
        if (result == Blocks.AIR)
            return Optional.empty();
        return Optional.of(BlockHelper.copyProperties(state, result.defaultBlockState()));
    }

    private Block processBlockUncached(Block block, Level level) {
        var result = process(new ItemStack(block), level);
        if (result == null || result.size() != 1)
            return Blocks.AIR;
        return Block.byItem(result.get(0).getItem());
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.getRandom().nextInt(8) == 0) {
            level.addParticle(new DustParticleOptions(this.variant.color(), 2),
                    pos.x + (level.getRandom().nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.getRandom().nextFloat() - .5f) * .5f,
                    0, 1 / 8f, 0);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(this.variant.color());
        particleAccess.setAlpha(1f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide())
            return;
        if (entity instanceof LivingEntity livingEntity)
            this.applyColoring(livingEntity, level);
        if (entity instanceof EnderMan || entity.getType() == EntityTypes.SNOW_GOLEM || entity.getType() == EntityTypes.BLAZE) {
            entity.hurt(entity.damageSources().drown(), 2);
        }
        if (entity.isOnFire()) {
            entity.clearFire();
            level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.NEUTRAL, 0.7F, 1.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.4F);
        }
    }

    private Optional<ItemStack> processByCrafting(ItemStack stack, Level level) {
        if (stack.is(CDPItems.MOD_TAGS.notApplicableColoring))
            return Optional.empty();

        var key = ItemStackKey.of(stack);
        var cached = craftingResultCache.get(key);
        if (cached != null)
            return cached.isEmpty() ? Optional.empty() : Optional.of(cached.copy());

        var result = processByCraftingUncached(stack, level);
        craftingResultCache.put(key, result.map(ItemStack::copy).orElse(ItemStack.EMPTY));
        return result;
    }

    private Optional<ItemStack> processByCraftingUncached(ItemStack stack, Level level) {
        // 1 Dye + 1 Colorless = 1 Dyed
        var dye = this.variant.dyeItemStack();
        if (dye.isEmpty())
            return Optional.empty();
        var input = CraftingInput.of(2, 1, List.of(stack, dye));
        var result = findAutomaticColoringCraftingResult(input, level, 1);
        if (result.isPresent())
            return result;
        // 1 Dye + 8 Colorless = 8 Dyed
        var items = NonNullList.withSize(9, stack);
        items.set(4, dye);
        input = CraftingInput.of(3, 3, items);
        result = findAutomaticColoringCraftingResult(input, level, 8);
        if (result.isPresent()) {
            var craftingResult = result.get();
            craftingResult.setCount(1);
            return Optional.of(craftingResult);
        }
        return Optional.empty();
    }

    private static Optional<ItemStack> findAutomaticColoringCraftingResult(CraftingInput input, Level level, int resultCount) {
        if (!(level instanceof ServerLevel serverLevel))
            return Optional.empty();
        for (var holder : serverLevel.recipeAccess().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe))
                continue;
            if (isIgnoredAutomaticColoringRecipe(recipe) || !recipe.matches(input, level))
                continue;
            var result = recipe.assemble(input);
            if (result.getCount() == resultCount)
                return Optional.of(result);
        }
        return Optional.empty();
    }

    private static boolean isIgnoredAutomaticColoringRecipe(CraftingRecipe recipe) {
        var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
        return SUPPLEMENTARIES_SUS_CRAFTING.equals(serializerId);
    }

    public void applyColoring(LivingEntity entity, Level level) {
        if (processColoring(entity)) {
            applyColoringImmediately(entity, level);
        }
    }

    public boolean applyContactColoring(ItemEntity entity, Level level) {
        if (level.isClientSide() || entity.isRemoved())
            return false;
        var processed = process(entity.getItem(), level);
        if (processed == null)
            return false;
        var stacks = new ArrayList<>(processed);
        if (stacks.isEmpty()) {
            entity.discard();
            return true;
        }
        entity.setItem(stacks.remove(0));
        for (ItemStack additional : stacks) {
            if (additional.isEmpty())
                continue;
            var additionalEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), additional.copy());
            additionalEntity.setDeltaMovement(entity.getDeltaMovement());
            level.addFreshEntity(additionalEntity);
        }
        return true;
    }

    public boolean applyContactColoring(LivingEntity entity, Level level) {
        if (level.isClientSide() || entity.isRemoved() || !canApplyContactColoring(entity))
            return false;
        return applyColoringImmediately(entity, level);
    }

    private boolean canApplyContactColoring(LivingEntity entity) {
        Integer previous = contactColoringTicks.put(entity.getUUID(), entity.tickCount);
        return previous == null || entity.tickCount - previous >= CONTACT_COLORING_COOLDOWN;
    }

    private boolean applyColoringImmediately(LivingEntity entity, Level level) {
        boolean changed = false;
        var vanillaColor = this.variant.vanillaColor();
        if (vanillaColor != null) {
            switch (entity) {
                case Sheep sheep -> {
                    if (sheep.getColor() != vanillaColor) {
                        sheep.setColor(vanillaColor);
                        changed = true;
                    }
                }
                default -> {}
            }
        }
        for (var slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty())
                continue;
            var result = this.applyColoring(stack, level);
            if (result.isPresent()) {
                var colored = result.get();
                colored.setCount(stack.getCount());
                entity.setItemSlot(slot, colored);
                changed = true;
            }
        }
        return changed;
    }

    private boolean processColoring(LivingEntity entity) {
        int processingTime = AllConfigs.server().kinetics.fanProcessingTime.get();
        ProcessingTimer previous = entityProcessing.get(entity.getUUID());
        if (previous == null || entity.tickCount - previous.lastTick() != 1) {
            entityProcessing.put(entity.getUUID(), new ProcessingTimer(entity.tickCount, processingTime));
            return false;
        }
        int remaining = previous.remaining() - 1;
        if (remaining <= 0) {
            entityProcessing.remove(entity.getUUID());
            return true;
        }
        entityProcessing.put(entity.getUUID(), new ProcessingTimer(entity.tickCount, remaining));
        return false;
    }

    private Optional<ItemStack> applyColoring(ItemStack stack, Level level) {
        var coloringInput = new ColoringRecipeInput(this.variant.id(), stack);
        if (!(level instanceof ServerLevel serverLevel))
            return this.processByCrafting(stack, level);
        var coloringRecipe = serverLevel.recipeAccess().getRecipeFor(CDPRecipes.COLORING.getType(), coloringInput, level);
        if (coloringRecipe.isPresent()) {
            ItemStack result = coloringRecipe.get().value().assemble(coloringInput);
            return Optional.of(result);
        }
        return this.processByCrafting(stack, level);
    }

    private record ProcessingTimer(int lastTick, int remaining) {}
}
