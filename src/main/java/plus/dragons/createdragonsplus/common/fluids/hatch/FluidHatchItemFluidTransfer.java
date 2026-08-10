/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.fluids.hatch;

import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Fabric-transfer implementation of direct item-to-tank fluid moves. */
public final class FluidHatchItemFluidTransfer {
    private FluidHatchItemFluidTransfer() {
    }

    public static TransferResult tryDrainItemToTank(
            Level level, ItemStack stack, FluidInventory tank, ServerFilteringBehaviour filter) {
        if (!GenericItemEmptying.canItemBeEmptied(level, stack)) {
            return TransferResult.EMPTY;
        }
        ItemStack working = stack.copyWithCount(1);
        var simulated = GenericItemEmptying.emptyItem(level, working, true);
        FluidStack fluid = simulated.getFirst();
        if (fluid.isEmpty() || !filter.test(fluid) || !tank.preciseInsert(fluid.copy())) {
            return TransferResult.EMPTY;
        }
        working = stack.copyWithCount(1);
        var result = GenericItemEmptying.emptyItem(level, working, false);
        if (result.getFirst().isEmpty()) {
            tank.extract(fluid);
            return TransferResult.EMPTY;
        }
        stack.shrink(1);
        return new TransferResult(fluid, result.getSecond());
    }

    public static TransferResult tryFillItemFromTank(
            Level level, ItemStack stack, FluidInventory tank, ServerFilteringBehaviour filter) {
        if (!GenericItemFilling.canItemBeFilled(level, stack)) {
            return TransferResult.EMPTY;
        }
        for (FluidStack stored : tank) {
            if (stored.isEmpty() || !filter.test(stored)) {
                continue;
            }
            int amount = GenericItemFilling.getRequiredAmountForItem(level, stack, stored);
            if (amount <= 0 || amount > stored.getAmount()) {
                continue;
            }
            ItemStack working = stack.copyWithCount(1);
            ItemStack result = GenericItemFilling.fillItem(level, amount, working, stored.copy());
            FluidStack moved = stored.copyWithAmount(amount);
            if (result.isEmpty() || !tank.preciseExtract(moved)) {
                continue;
            }
            stack.shrink(1);
            return new TransferResult(moved, result);
        }
        return TransferResult.EMPTY;
    }

    public static boolean canItemBeFilled(Level level, ItemStack stack) {
        return GenericItemFilling.canItemBeFilled(level, stack);
    }

    public static boolean canItemBeEmptied(Level level, ItemStack stack) {
        return GenericItemEmptying.canItemBeEmptied(level, stack);
    }

    public record TransferResult(FluidStack fluidStack, ItemStack result) {
        public static final TransferResult EMPTY = new TransferResult(FluidStack.EMPTY, ItemStack.EMPTY);

        public boolean isEmpty() {
            return fluidStack.isEmpty();
        }
    }
}
