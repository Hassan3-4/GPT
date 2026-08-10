/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.client.behaviour;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import java.util.List;
import net.minecraft.network.chat.Component;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;

/** Connects CEI's common experience tooltip data to Create Fly's client tooltip registry. */
public final class BlazeExperienceTooltipBehaviour<T extends BlazeExperienceBlockEntity>
        extends TooltipBehaviour<T> implements IHaveGoggleInformation {
    public BlazeExperienceTooltipBehaviour(T blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return blockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
