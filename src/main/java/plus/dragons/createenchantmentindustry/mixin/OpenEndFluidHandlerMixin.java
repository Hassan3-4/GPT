/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.mixin;

import com.zurrtum.create.content.fluids.OpenEndedPipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceEffectHandler;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

/**
 * Restores CEI's consuming open-pipe effect on Create Fly.
 *
 * <p>Create Fly's normal effect callback receives the complete amount held by
 * an open pipe after every insertion. Treating that amount as a delta awards
 * the same liquid repeatedly and eventually leaves a placeable source block.
 * Liquid Experience must instead be consumed as it enters the open end.</p>
 */
@Mixin(targets = "com.zurrtum.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler")
public abstract class OpenEndFluidHandlerMixin {
    @Shadow
    private FluidStack stack;

    @Shadow
    private int previousAmount;

    @Shadow
    @Final
    private OpenEndedPipe this$0;

    @Inject(method = "markDirty", at = @At("HEAD"), cancellable = true)
    private void cei$consumeLiquidExperience(CallbackInfo ci) {
        int amount = stack.getAmount();
        if (amount <= previousAmount
                || !CEIFluids.EXPERIENCE.isSame(stack.getFluid())
                || !(this$0.getWorld() instanceof ServerLevel)) {
            return;
        }

        int experience = FluidUnits.toMillibuckets(amount);
        int remainder = amount % FluidUnits.PER_MILLIBUCKET;
        if (experience > 0) {
            ExperienceEffectHandler.award(this$0.getWorld(), this$0.getAOE(), experience);
        }

        // Keep sub-mB units in the handler so repeated small transfers remain
        // lossless, but never accumulate enough fluid to place a source block.
        stack = remainder == 0 ? FluidStack.EMPTY : stack.copyWithAmount(remainder);
        previousAmount = remainder;
        ci.cancel();
    }
}
