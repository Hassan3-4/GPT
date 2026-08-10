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

package plus.dragons.createenchantmentindustry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.deployer.DeployerFakePlayer;
import com.zurrtum.create.content.kinetics.deployer.DeployerHandler;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.createenchantmentindustry.common.kinetics.deployer.DeployerExtension;

@Mixin(DeployerHandler.class)
public class DeployerHandlerMixin {
    @WrapOperation(method = "tryHarvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V"))
    private static void createEnchantmentIndustry$captureBlockExperience(BlockState state, ServerLevel level, BlockPos pos,
                                                                          ItemStack stack, boolean dropExperience,
                                                                          Operation<Void> original,
                                                                          @Local(argsOnly = true) DeployerPlayer player) {
        if (player instanceof DeployerFakePlayer deployer) {
            DeployerExtension.withBlockBreaker(deployer, () -> {
                original.call(state, level, pos, stack, dropExperience);
                return null;
            });
        } else {
            original.call(state, level, pos, stack, dropExperience);
        }
    }
}
