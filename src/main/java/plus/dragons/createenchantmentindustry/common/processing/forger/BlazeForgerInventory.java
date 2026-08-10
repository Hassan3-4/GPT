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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class BlazeForgerInventory extends ItemStackHandler {
    private static final Codec<SlotStack> SLOT_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BYTE.fieldOf("Slot").forGetter(entry -> (byte) entry.slot()),
        ItemStack.MAP_CODEC.forGetter(SlotStack::stack)
    ).apply(instance, (slot, stack) -> new SlotStack(Byte.toUnsignedInt(slot), stack)));

    private final BlazeForgerBlockEntity forger;
    private int cost;
    private int mode; // Advancement Flag. 0 = merge item 1 = apply template 2 = strip down enchantment
    private boolean conflicting; // Advancement Flag
    private boolean overCap; // Advancement Flag

    public BlazeForgerInventory(BlazeForgerBlockEntity forger) {
        super(6);
        this.forger = forger;
        this.mode = 0;
        this.conflicting = false;
        this.overCap = false;
    }

    public int getSlotLimit(int slot) {
        return 1;
    }

    public int getSlots() {
        return 4;
    }

    @Override
    public int getContainerSize() {
        return getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot, stacks.size());
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot, stacks.size());
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot, getSlots());
        if (slot > 1) return stack;
        if (!stacks.get(2).isEmpty() || !stacks.get(3).isEmpty()) return stack;
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack existing = stacks.get(slot);
        if (!existing.isEmpty()) return stack;
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        if (!simulate) {
            stacks.set(slot, stack.copyWithCount(1));
            onContentsChanged(slot);
        }
        return remainder;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot, getSlots());
        if (amount <= 0) return ItemStack.EMPTY;
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;
        int extractedAmount = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copyWithCount(extractedAmount);
        if (!simulate) {
            if (extractedAmount == existing.getCount()) stacks.set(slot, ItemStack.EMPTY);
            else existing.shrink(extractedAmount);
            onContentsChanged(slot);
        }
        return extracted;
    }

    protected void onLoad() {
        var level = forger.getLevel();
        if (level != null && !level.isClientSide())
            updateResult();
    }

    protected void onContentsChanged(int slot) {
        if (slot == 0 || slot == 1)
            updateResult();
        forger.notifyUpdate();
    }

    public void read(ValueInput view) {
        for (int i = 0; i < stacks.size(); i++) stacks.set(i, ItemStack.EMPTY);
        for (SlotStack entry : view.listOrEmpty("Items", SLOT_STACK_CODEC)) {
            int slot = entry.slot();
            if (slot < 0 || slot >= getSlots()) continue;
            stacks.set(slot, entry.stack());
        }
        cost = view.getIntOr("Cost", 0);
        mode = view.getIntOr("Mode", 0);
        conflicting = view.getBooleanOr("Conflicting", false);
        overCap = view.getBooleanOr("OverCap", false);
        onLoad();
    }

    public void write(ValueOutput view) {
        view.putInt("Size", getSlots());
        var items = view.list("Items", SLOT_STACK_CODEC);
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;
            items.add(new SlotStack(slot, stack));
        }
        view.putInt("Cost", cost);
        view.putInt("Mode", mode);
        view.putBoolean("Conflicting", conflicting);
        view.putBoolean("OverCap", overCap);
    }

    public boolean hasRemainingOutput() {
        return !stacks.get(2).isEmpty() || !stacks.get(3).isEmpty();
    }

    protected int getExperienceCost() {
        return cost == 0 ? 0 : ExperienceHelper.getExperienceForTotalLevel(cost);
    }

    protected ItemStack extractInput(int slot, boolean simulate) {
        validateSlotIndex(slot, getSlots());
        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!simulate)
            setStackInSlot(slot, ItemStack.EMPTY);
        return stack.copy();
    }

    protected ItemStack getResult(int slot) {
        if (slot < 0 || slot >= 2) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0,2)");
        }
        return stacks.get(slot + 4);
    }

    protected void clearInput() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        cost = 0;
    }

    protected void clear() {
        for (int i = 0; i < stacks.size(); i++) stacks.set(i, ItemStack.EMPTY);
        cost = 0;
    }

    private static void validateSlotIndex(int slot, int size) {
        if (slot < 0 || slot >= size)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
    }

    protected void applyResult() {
        stacks.set(2, stacks.get(4).copy());
        stacks.set(3, stacks.get(5).copy());
        clearInput();

        forger.advancement.awardStat(CEIStats.FORGE.get(), 1);
        if (forger.special) {
            forger.advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
            if (overCap) forger.advancement.trigger(CEIAdvancements.TRANSCENDENT_OVERCLOCK.builtinTrigger());
            if (conflicting) forger.advancement.trigger(CEIAdvancements.PARADOX_FUSION.builtinTrigger());
        }
        forger.advancement.trigger(mode == 0 ? CEIAdvancements.BLAZING_FUSION.builtinTrigger() : mode == 1 ? CEIAdvancements.SIGIL_CASTING.builtinTrigger() : CEIAdvancements.MAGIC_UNBINDING.builtinTrigger());
    }

    protected void updateResult() {
        var base = stacks.get(0).copy();
        var addition = stacks.get(1).copy();
        stacks.set(4, base);
        stacks.set(5, addition);
        // Recalculate from a clean state. The blaze can change between regular
        // and Super Forging while both inputs remain inserted; retaining the
        // old preview cost would otherwise let a now-invalid template process.
        cost = 0;
        mode = 0;
        conflicting = false;
        overCap = false;
        if (base.isEmpty() || addition.isEmpty()) {
            return;
        }
        var baseType = CEIEnchantmentHelper.getEnchantmentComponentType(base);
        var baseEnchantments = CEIEnchantmentHelper.getEnchantments(base);
        var additionType = CEIEnchantmentHelper.getEnchantmentComponentType(addition);
        var additionEnchantments = CEIEnchantmentHelper.getEnchantments(addition);
        if (baseType == DataComponents.STORED_ENCHANTMENTS) {
            if (base.getItem() instanceof EnchantingTemplateItem baseTemplate) {
                if (addition.getItem() instanceof EnchantingTemplateItem addTemplate) {
                    if (baseTemplate.isSpecial() != forger.special || addTemplate.isSpecial() != forger.special) return;
                    else {
                        if (additionEnchantments.isEmpty()) {
                            if (!splitEnchantments(base, addition, baseEnchantments, additionEnchantments)) return;
                        } else {
                            if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments))
                                stacks.set(5, ItemStack.EMPTY);
                            else return;
                        }
                    }
                }
            } else if (base.is(Items.ENCHANTED_BOOK)) {
                if (addition.getItem() instanceof EnchantingTemplateItem template) {
                    if (template.isSpecial() != forger.special) return;
                    if (additionEnchantments.isEmpty()) {
                        if (baseEnchantments.size() == 1) {
                            var book = Items.BOOK.getDefaultInstance();
                            CEIEnchantmentHelper.setEnchantments(addition, baseEnchantments);
                            stacks.set(4, book);
                            stacks.set(5, addition);
                            var enchantment = baseEnchantments.entrySet().stream().findFirst().get();
                            cost += Math.max(1, enchantment.getKey().value().getAnvilCost() / 2) * enchantment.getIntValue();
                        } else if (!splitEnchantments(base, addition, baseEnchantments, additionEnchantments)) return;
                    } else {
                        if (applyEnchantments(base, baseEnchantments, additionEnchantments)) {
                            stacks.set(5, ItemStack.EMPTY);
                        } else return;
                    }
                } else if (addition.is(Items.ENCHANTED_BOOK)) {
                    if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                        stacks.set(5, ItemStack.EMPTY);
                    } else return;
                } else return;
            }
        } else if (base.is(Items.BOOK) && addition.getItem() instanceof EnchantingTemplateItem template) {
            if (template.isSpecial() != forger.special) return;
            if (additionEnchantments.isEmpty()) return;
            else {
                if (applyEnchantmentsToBook(base, additionEnchantments))
                    stacks.set(5, ItemStack.EMPTY);
                else return;
            }
        } else {
            if (addition.getItem() instanceof EnchantingTemplateItem template) {
                if (template.isSpecial() != forger.special) return;
                if (additionEnchantments.isEmpty()) {
                    if (baseEnchantments.isEmpty()) return;
                    if (!splitEnchantments(base, addition, baseEnchantments, additionEnchantments)) return;
                } else {
                    if (applyEnchantments(base, baseEnchantments, additionEnchantments)) {
                        stacks.set(5, ItemStack.EMPTY);
                    } else return;
                }
            } else if (addition.is(Items.ENCHANTED_BOOK)) {
                if (applyEnchantments(base, baseEnchantments, additionEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                } else return;
            } else if (ItemStack.isSameItem(base, addition)) {
                if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                } else return;
            } else return;
        }
        applyRepairCost(base, addition);
    }

    protected boolean splitEnchantments(ItemStack base, ItemStack addition, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        mode = 2;
        if (baseEnchantments.isEmpty())
            return false;
        var registry = Objects.requireNonNull(forger.getLevel()).registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var stream = baseEnchantments.keySet().stream().sorted(Comparator.comparingInt(holder -> registry.getId(holder.value())));
        if (!forger.special) {
            stream = stream.filter(holder -> !holder.is(EnchantmentTags.CURSE));
        }
        var optional = stream.findFirst();
        if (optional.isEmpty())
            return false;
        var enchantment = optional.get();
        var removedEnchantments = new ItemEnchantments.Mutable(baseEnchantments);
        removedEnchantments.set(enchantment, 0);
        CEIEnchantmentHelper.setEnchantments(base, removedEnchantments.toImmutable());
        int level = baseEnchantments.getLevel(enchantment);
        if (!forger.special)
            level = Math.min(level, CEIEnchantmentHelper.maxLevel(enchantment) + (CEIConfig.enchantments().splitEnchantmentRespectLevelExtension.get() ? CEIEnchantmentHelper.levelExtension(enchantment) : 0));
        CEIEnchantmentHelper.addEnchantment(addition, enchantment, level);
        var multiplier = CEIDataMaps.SPLITTING_COST_MULTIPLIER.get(enchantment);
        cost += (int) (Math.max(1, enchantment.value().getAnvilCost() / 2) * level * (multiplier != null ? multiplier : 1));
        return true;
    }

    protected boolean applyEnchantments(ItemStack base, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        mode = 1;
        int cost = 0;
        var resultEnchantments = new Mutable(baseEnchantments);
        boolean applied = false;
        for (Entry<Holder<Enchantment>> entry : additionEnchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int baseLevel = resultEnchantments.getLevel(holder);
            int additionLevel = entry.getIntValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);
            Enchantment enchantment = holder.value();
            boolean applicable = holder.value().isSupportedItem(base);
            for (Holder<Enchantment> holder1 : resultEnchantments.keySet()) {
                if (!holder1.equals(holder) && !Enchantment.areCompatible(holder, holder1)) {
                    applicable = forger.special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get();
                    conflicting = true;
                    cost++;
                }
            }

            if (applicable) {
                applied = true;
                int maxLevel = CEIEnchantmentHelper.maxLevel(holder);
                int extendedMaxLevel = maxLevel + CEIEnchantmentHelper.levelExtension(holder);

                if (resultLevel > extendedMaxLevel) {
                    resultLevel = extendedMaxLevel;
                } else if (resultLevel > maxLevel && !forger.special) {
                    resultLevel = maxLevel;
                }
                if (resultLevel > maxLevel) overCap = true;

                resultEnchantments.set(holder, resultLevel);
                int anvilCost = enchantment.getAnvilCost();
                anvilCost = Math.max(1, anvilCost / 2);

                var multiplier = CEIDataMaps.FORGING_COST_MULTIPLIER.get(holder);
                cost += (int) (anvilCost * resultLevel * (multiplier != null ? multiplier : 1));
            }
        }
        if (!applied)
            return false;
        CEIEnchantmentHelper.setEnchantments(base, resultEnchantments.toImmutable());
        this.cost += cost;
        return true;
    }

    protected boolean applyEnchantmentsToBook(ItemStack base, ItemEnchantments additionEnchantments) {
        mode = 1;
        int cost = 0;
        var resultEnchantments = new Mutable(ItemEnchantments.EMPTY);
        boolean applied = false;
        for (Entry<Holder<Enchantment>> entry : additionEnchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();
            boolean applicable = true;
            for (Holder<Enchantment> holder1 : resultEnchantments.keySet()) {
                if (!holder1.equals(holder) && !Enchantment.areCompatible(holder, holder1)) {
                    applicable = forger.special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get();
                    conflicting = applicable;
                    cost++;
                }
            }
            if (applicable) {
                applied = true;
                resultEnchantments.set(holder, entry.getIntValue());
                int anvilCost = enchantment.getAnvilCost();
                anvilCost = Math.max(1, anvilCost / 2);
                var multiplier = CEIDataMaps.FORGING_COST_MULTIPLIER.get(holder);
                cost += (int) (anvilCost * entry.getIntValue() * (multiplier != null ? multiplier : 1));
            }
        }
        if (!applied)
            return false;
        base = Items.ENCHANTED_BOOK.getDefaultInstance();
        CEIEnchantmentHelper.setEnchantments(base, resultEnchantments.toImmutable());
        stacks.set(4, base);
        this.cost += cost;
        return true;
    }

    protected boolean combineEnchantments(ItemStack base, ItemStack addition, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        boolean applied = false;
        if (base.isDamaged()) {
            int baseDurability = base.getMaxDamage() - base.getDamageValue();
            int additionDurability = addition.getMaxDamage() - addition.getDamageValue();
            int fix = additionDurability + base.getMaxDamage() * 12 / 100;
            int resultDurability = baseDurability + fix;
            int resultDamage = base.getMaxDamage() - resultDurability;
            if (resultDamage < 0) {
                resultDamage = 0;
            }

            if (resultDamage < base.getDamageValue()) {
                base.setDamageValue(resultDamage);
                cost += 2;
                applied = true;
            }
        }
        applied |= applyEnchantments(base, baseEnchantments, additionEnchantments);
        mode = 0;
        return applied;
    }

    protected void applyRepairCost(ItemStack base, ItemStack addition) {
        if (!forger.cursed)
            return;
        int baseCost = base.getOrDefault(DataComponents.REPAIR_COST, 0);
        int additionCost = addition.getOrDefault(DataComponents.REPAIR_COST, 0);
        int resultCost = AnvilMenu.calculateIncreasedRepairCost(Math.max(baseCost, additionCost));
        base.set(DataComponents.REPAIR_COST, resultCost);
    }

    boolean forgingCompleted() {
        return !stacks.get(2).isEmpty() && forger.processingTime == -1;
    }

    boolean notEnoughItemToForge() {
        return stacks.get(0).isEmpty() || stacks.get(1).isEmpty();
    }

    boolean incompatibleEnchantingTemplateType() {
        var base = stacks.get(0);
        var addition = stacks.get(1);
        if (!forger.special && (base.is(CEIItems.SUPER_ENCHANTING_TEMPLATE.get()) || addition.is(CEIItems.SUPER_ENCHANTING_TEMPLATE.get())))
            return true;
        else return forger.special && (base.is(CEIItems.ENCHANTING_TEMPLATE.get()) || addition.is(CEIItems.ENCHANTING_TEMPLATE.get()));
    }

    private record SlotStack(int slot, ItemStack stack) {}
}
