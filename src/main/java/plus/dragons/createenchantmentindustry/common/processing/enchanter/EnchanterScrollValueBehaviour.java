/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Client-side value-box facade for the server-authoritative enchanting behaviour. */
public class EnchanterScrollValueBehaviour
        extends ScrollValueBehaviour<BlazeEnchanterBlockEntity, EnchanterBehaviour> {

    public EnchanterScrollValueBehaviour(BlazeEnchanterBlockEntity blockEntity, ValueBoxTransform transform) {
        super(CEILang.translate("gui.blaze_enchanter.level").component(), blockEntity, transform);
        behaviour = blockEntity.getEnchanterBehaviour();
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int max = blockEntity.getMaxEnchantLevel();
        return new ValueSettingsBoard(
                label,
                max,
                max / 6,
                ImmutableList.of(label),
                new ValueSettingsFormatter());
    }

    /**
     * An enchantable item is the enchanter's template filter, not a request to
     * open the numeric level board.  Handling it immediately also mirrors
     * Create's ordinary filtering behaviour and guarantees that the matching
     * server packet is sent on the same click.  Empty-hand interaction keeps
     * the short-click/long-hold split used to clear the filter or edit levels.
     */
    @Override
    public boolean acceptsValueSettings() {
        var player = Minecraft.getInstance().player;
        return player == null || !player.getMainHandItem().isEnchantable();
    }

    /** Client-only position of the enchanting-level value box. */
    public static class EnchanterTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 13.5);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
