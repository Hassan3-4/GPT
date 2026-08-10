/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.fluids.tank;

import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Create Fly's fluid-inventory equivalent of CDP's former configurable Forge
 * tank.  The predicates are enforced for all Fabric inventory operations,
 * rather than only for a capability wrapper.
 */
public class ConfigurableFluidTank extends FluidTank {
    private Predicate<FluidStack> insertion = stack -> true;
    private Predicate<FluidStack> extraction = stack -> true;
    private final Consumer<FluidStack> updateCallback;

    public ConfigurableFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity);
        this.updateCallback = Objects.requireNonNull(updateCallback, "updateCallback");
    }

    public ConfigurableFluidTank allowInsertion() {
        insertion = stack -> true;
        return this;
    }

    public ConfigurableFluidTank allowInsertion(Predicate<FluidStack> predicate) {
        insertion = Objects.requireNonNull(predicate, "predicate");
        return this;
    }

    public ConfigurableFluidTank forbidInsertion() {
        insertion = stack -> false;
        return this;
    }

    public ConfigurableFluidTank allowExtraction() {
        extraction = stack -> true;
        return this;
    }

    /** Kept with the original public spelling for binary/source API continuity. */
    public ConfigurableFluidTank allowExtration(Predicate<FluidStack> predicate) {
        extraction = Objects.requireNonNull(predicate, "predicate");
        return this;
    }

    public ConfigurableFluidTank allowExtraction(Predicate<FluidStack> predicate) {
        return allowExtration(predicate);
    }

    public ConfigurableFluidTank forbidExtraction() {
        extraction = stack -> false;
        return this;
    }

    @Override
    public boolean isValid(int slot, FluidStack stack) {
        return slot == 0 && insertion.test(stack);
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        return insertion.test(stack) ? super.insert(stack, maxAmount) : 0;
    }

    @Override
    public boolean preciseInsert(FluidStack stack, int maxAmount) {
        return insertion.test(stack) && super.preciseInsert(stack, maxAmount);
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        return extraction.test(fluid) ? super.extract(stack, maxAmount) : 0;
    }

    @Override
    public FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
        return extraction.test(fluid) ? super.extract(predicate, maxAmount) : FluidStack.EMPTY;
    }

    @Override
    public FluidStack extractAny(int maxAmount) {
        return extraction.test(fluid) ? super.extractAny(maxAmount) : FluidStack.EMPTY;
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        return extraction.test(fluid) && super.preciseExtract(stack);
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        updateCallback.accept(getFluid());
    }
}
