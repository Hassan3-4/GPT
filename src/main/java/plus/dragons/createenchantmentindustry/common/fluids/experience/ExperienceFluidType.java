/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/**
 * Compatibility home for Liquid Experience's placement behaviour.  Fabric
 * has no NeoForge FluidType; rendering is registered client-side and this
 * class preserves the configurable vapourisation rule used by the bucket.
 */
public final class ExperienceFluidType {
    private ExperienceFluidType() {}

    public static boolean vaporizesOnPlacement() {
        return CEIConfig.server().fluids.experienceVaporizeOnPlacement.get();
    }

    public static void vaporize(@Nullable LivingEntity user, Level level, BlockPos pos, int amount) {
        level.playSound(user, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, pos.getCenter(), amount);
        }
    }
}
