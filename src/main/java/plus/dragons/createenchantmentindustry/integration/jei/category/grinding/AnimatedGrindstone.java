/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.integration.jei.category.grinding;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** PIP-backed JEI animation compatible with the 1.21.11 GUI renderer. */
public class AnimatedGrindstone implements IDrawable {
    public int offset;
    private final BlockState grindstone = CEIBlocks.MECHANICAL_GRINDSTONE.get().defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
    private final BlockState drain = CEIBlocks.GRINDSTONE_DRAIN.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        float angle = (AnimationTickHolder.getRenderTime() * 4f) % 360;
        var drainElement = GuiGameElement.of(drain);
        drainElement.at(xOffset + 2, yOffset + 22);
        drainElement.rotate(-15.5f, 22.5f, 0);
        drainElement.scale(25);
        drainElement.render(graphics);

        var grindstoneElement = GuiGameElement.of(grindstone);
        grindstoneElement.at(xOffset + 2, yOffset - 3);
        grindstoneElement.rotate(-15.5f, 22.5f, angle);
        grindstoneElement.scale(25);
        grindstoneElement.render(graphics);
    }
}
