/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.client.behaviour;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Restores CEI's experience-point amount board on Create Fly's split client behaviour API. */
public final class ExperienceHatchClientBehaviour extends FilteringBehaviour<ExperienceHatchBehaviour> {
    public ExperienceHatchClientBehaviour(SmartBlockEntity blockEntity, ValueBoxTransform transform) {
        super(blockEntity, transform);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                CEILang.translate("gui.experience_hatch.exchange").component(),
                100,
                10,
                List.of(CEILang.translate("gui.experience_hatch.points").component()),
                new ValueSettingsFormatter(this::formatValue));
    }

    @Override
    public MutableComponent formatValue(com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings value) {
        int count = value.value();
        if (count == 0)
            return CEILang.translate("gui.experience_hatch.all").component();
        return Component.literal(String.valueOf(count * ExperienceHatchBehaviour.POINTS_PER_SCROLL));
    }

    @Override
    public MutableComponent getCountLabelForValueBox() {
        int count = behaviour.getValueSettings().value();
        if (count == 0)
            return Component.literal("*");
        return Component.literal(String.valueOf(count * ExperienceHatchBehaviour.POINTS_PER_SCROLL));
    }
}
