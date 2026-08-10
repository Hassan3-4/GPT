/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Bucket behaviour for the configurable "vaporize into XP" CEI feature. */
public final class ExperienceBucketItem extends BucketItem {
    public ExperienceBucketItem(Fluid content, Item.Properties properties) {
        super(content, properties);
    }

    @Override
    public boolean emptyContents(@Nullable LivingEntity user, Level level, BlockPos pos, @Nullable BlockHitResult hitResult) {
        if (!ExperienceFluidType.vaporizesOnPlacement()) {
            return super.emptyContents(user, level, pos, hitResult);
        }

        if (!level.isClientSide()) {
            ExperienceFluidType.vaporize(user, level, pos, 1000);
        }
        return true;
    }
}
