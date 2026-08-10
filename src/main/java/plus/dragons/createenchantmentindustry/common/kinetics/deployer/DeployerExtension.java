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

package plus.dragons.createenchantmentindustry.common.kinetics.deployer;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.deployer.DeployerFakePlayer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public final class DeployerExtension {
    private static final ThreadLocal<DeployerFakePlayer> BLOCK_BREAKER = new ThreadLocal<>();

    private DeployerExtension() {}

    public static <T> T withBlockBreaker(DeployerFakePlayer deployer, Supplier<T> action) {
        DeployerFakePlayer previous = BLOCK_BREAKER.get();
        BLOCK_BREAKER.set(deployer);
        try {
            return action.get();
        } finally {
            if (previous == null) BLOCK_BREAKER.remove();
            else BLOCK_BREAKER.set(previous);
        }
    }

    /** Returns true when CEI handled a deployer-generated block XP award. */
    public static boolean handleBlockExperience(ServerLevel level, BlockPos pos, int vanillaExperience) {
        DeployerFakePlayer deployer = BLOCK_BREAKER.get();
        if (deployer == null) return false;
        int experience = CEIConfig.kinetics().deployerMineDropXp.get()
            ? Mth.ceil(vanillaExperience * CEIConfig.kinetics().deployerMineXpScale.getF())
            : 0;
        awardOrCollect(deployer, level, Vec3.atCenterOf(pos), experience);
        return true;
    }

    public static void handleKillExperience(DeployerFakePlayer deployer, ServerLevel level, Vec3 pos, int vanillaExperience) {
        int experience = Mth.ceil(vanillaExperience * CEIConfig.kinetics().deployerKillXpScale.getF());
        awardOrCollect(deployer, level, pos, experience);
    }

    private static void awardOrCollect(DeployerFakePlayer deployer, ServerLevel level, Vec3 pos, int experience) {
        if (experience <= 0) return;
        if (CEIConfig.kinetics().deployerCollectXp.get()) collectExperience(deployer, experience);
        else ExperienceOrb.award(level, pos, experience);
    }

    public static void collectExperience(DeployerFakePlayer deployer, int experience) {
        if (experience <= 0)
            return;
        if (CEIConfig.kinetics().deployerMendItem.get()) {
            ItemStack heldItem = deployer.getMainHandItem();
            if (ExperienceHelper.canRepairItem(heldItem))
                experience -= ExperienceHelper.repairItem(experience, deployer.level(), heldItem, false);
        }
        if (experience <= 0)
            return;
        int nuggets = experience / 3;
        if (deployer.level().random.nextFloat() < (experience % 3) / 3f)
            nuggets++;
        if (nuggets > 0) {
            deployer.getInventory().placeItemBackInInventory(new ItemStack(AllItems.EXP_NUGGET, nuggets));
        }
    }
}
