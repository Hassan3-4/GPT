/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** PIP-backed printer animation for Minecraft's 1.21.11 GUI submission system. */
public class AnimatedPrinter implements IDrawable {
    public int offset;
    private FluidStack fluid = FluidStack.EMPTY;

    public AnimatedPrinter withFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }

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
        int scale = 20;
        var printerElement = GuiGameElement.of(CEIBlocks.PRINTER.get().defaultBlockState());
        printerElement.at(xOffset, yOffset);
        printerElement.rotate(-15.5f, 22.5f, 0);
        printerElement.scale(scale);
        printerElement.render(graphics);

        var depotElement = GuiGameElement.of(AllBlocks.DEPOT.defaultBlockState());
        depotElement.at(xOffset, yOffset + 28);
        depotElement.rotate(-15.5f, 22.5f, 0);
        depotElement.scale(scale);
        depotElement.render(graphics);

        float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
        float progress = cycle < 20 ? -PrinterRenderer.getProgress(cycle / 20f * 50f) : 0;
        renderPartial(graphics, CEIPartialModels.PRINTER_NOZZLE_TOP, xOffset, yOffset, scale, 3 * progress / 32f);
        renderPartial(graphics, CEIPartialModels.PRINTER_NOZZLE_BOTTOM, xOffset, yOffset, scale, 6 * progress / 32f);
        renderPartial(graphics, CEIPartialModels.PRINTER_PISTON, xOffset, yOffset, scale, -progress / 2f);

        if (!fluid.isEmpty() && fluid.getFluid().getBucket() != null) {
            ItemStack bucket = new ItemStack(fluid.getFluid().getBucket());
            if (!bucket.isEmpty()) {
                var bucketElement = GuiGameElement.of(bucket);
                bucketElement.at(xOffset + 18, yOffset + 18);
                bucketElement.scale(.75f);
                bucketElement.render(graphics);
            }
        }
    }

    private static void renderPartial(
            GuiGraphics graphics,
            com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel model,
            int x,
            int y,
            int scale,
            float localY) {
        var element = GuiGameElement.of(model);
        element.at(x, y);
        element.atLocal(0, localY);
        element.scale(scale);
        element.transform((poseStack, partialTicks) -> {
            poseStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
            poseStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        });
        element.render(graphics);
    }
}
