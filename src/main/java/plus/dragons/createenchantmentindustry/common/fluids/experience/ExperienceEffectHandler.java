/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

/** Converts Liquid Experience emitted by an open Create pipe into experience. */
public final class ExperienceEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (fluid.isEmpty()) {
            return;
        }

        int amount = ExperienceHelper.getExperienceFromFluid(fluid);
        award(level, area, amount);
    }

    public static void award(Level level, AABB area, int amount) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (amount <= 0) {
            return;
        }

        // Fabric has no loader-level FakePlayer base class.  The server player
        // query therefore includes only actual ServerPlayer entities supplied
        // by the running game, which is the portable equivalent here.
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area, player -> true);
        if (players.isEmpty()) {
            ExperienceOrb.award(serverLevel, area.getCenter(), amount);
            return;
        }

        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        ExperienceHelper.award(amount, player);
        CEIAdvancements.A_SHOWER_EXPERIENCE.awardTo(player);
    }
}
