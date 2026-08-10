/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.kinetics.deployer.DeployerExtension;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "popExperience", at = @At("HEAD"), cancellable = true)
    private void createEnchantmentIndustry$handleDeployerExperience(ServerLevel level, BlockPos pos, int amount,
                                                                    CallbackInfo ci) {
        if (DeployerExtension.handleBlockExperience(level, pos, amount)) ci.cancel();
    }
}
