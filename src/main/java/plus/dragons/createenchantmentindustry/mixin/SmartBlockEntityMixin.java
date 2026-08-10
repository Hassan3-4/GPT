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

import com.zurrtum.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFluidDropContext;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;

@Mixin(SmartBlockEntity.class)
public abstract class SmartBlockEntityMixin extends CachedRenderBBBlockEntity {
    @Shadow
    public abstract <T extends BlockEntityBehaviour> @Nullable T getBehaviour(BehaviourType<T> type);

    @Shadow
    public abstract Collection<BlockEntityBehaviour> getAllBehaviours();

    public SmartBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "destroy", at = @At(value = "HEAD"))
    private void setRemoved$dropExperienceFluid(CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;
        if (!ExperienceFluidDropContext.shouldDropExperienceFluid(serverLevel))
            return;
        for (var behaviour : this.getAllBehaviours()) {
            FluidInventory handler = null;
            if (behaviour instanceof SmartFluidTankBehaviour tank) {
                handler = tank.getCapability();
            } else if (behaviour.getClass().getName().equals(
                "plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour")) {
                try {
                    Object capability = behaviour.getClass().getMethod("getCapability").invoke(behaviour);
                    if (capability instanceof FluidInventory inventory) handler = inventory;
                } catch (ReflectiveOperationException ignored) {
                    // The optional CDP integration is absent or uses an incompatible API.
                }
            }
            if (handler == null) continue;
            for (int tank = 0; tank < handler.size(); tank++) {
                var fluid = handler.getStack(tank);
                int experience = ExperienceHelper.getExperienceFromFluid(fluid);
                if (experience > 0) {
                    ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.worldPosition), experience);
                }
            }
        }
    }
}
