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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

public class ExperienceHelper {
    public static int getExperienceForNextLevel(int level) {
        if (level >= 30)
            return 9 * level - 158;
        if (level >= 15)
            return 5 * level - 38;
        return 2 * level + 7;
    }

    public static int getExperienceForTotalLevel(int level) {
        if (level == 0)
            return 0;
        if (level >= 31)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 16)
            return (5 * level * level - 81 * level) / 2 + 360;
        return level * level + 6 * level;
    }

    public static int getExperienceForPlayer(Player player) {
        int experience = getExperienceForTotalLevel(player.experienceLevel);
        experience += Math.round(player.experienceProgress * getExperienceForNextLevel(player.experienceLevel));
        return experience;
    }

    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return 0;
        if (CEIFluids.EXPERIENCE.isSame(fluid.getFluid()))
            return FluidUnits.toMillibuckets(fluid.getAmount());
        int amount = fluid.getAmount();
        Integer unit = CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid.getFluid());
        if (unit == null)
            return 0;
        return amount / unit;
    }

    public static int getFluidFromExperience(FluidStack fluid, int amount) {
        return getFluidFromExperience(fluid.getRegistryEntry(), amount);
    }

    public static int getFluidFromExperience(Holder<Fluid> fluid, int amount) {
        return getExperienceFluidUnit(fluid) * amount;
    }

    public static int getExperienceFluidUnit(Holder<Fluid> fluid) {
        if (CEIFluids.EXPERIENCE.isSame(fluid.value()))
            return FluidUnits.PER_MILLIBUCKET;
        Integer unit = CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid);
        return unit == null ? 0 : unit;
    }

    public static void award(int amount, ServerPlayer player) {
        amount = repairPlayerItems(player, amount);
        player.giveExperiencePoints(amount);
    }

    public static boolean canRepairItem(ItemStack stack) {
        if (!stack.isDamaged())
            return false;
        ItemEnchantments enchantments = stack.getEnchantments();
        for (var enchantment : enchantments.keySet()) {
            if (enchantment.value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP))
                return true;
        }
        return false;
    }

    public static int repairItem(int amount, ServerLevel level, ItemStack stack, boolean simulate) {
        int repairing = EnchantmentHelper.modifyDurabilityToRepairFromXp(level, stack, amount);
        int repaired = Math.min(repairing, stack.getDamageValue());
        if (repaired == 0)
            return 0;
        if (!simulate) {
            stack.setDamageValue(stack.getDamageValue() - repaired);
        }
        return Math.max(1, repaired * amount / repairing);
    }

    public static int repairPlayerItems(ServerPlayer player, int amount) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            ItemStack stack = optional.get().itemStack();
            int consumed = repairItem(amount, player.level(), stack, false);
            return amount > consumed
                    ? repairPlayerItems(player, amount - consumed)
                    : 0;
        }
        return amount;
    }
}
