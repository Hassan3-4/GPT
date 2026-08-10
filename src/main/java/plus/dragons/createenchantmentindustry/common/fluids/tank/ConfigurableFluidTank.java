package plus.dragons.createenchantmentindustry.common.fluids.tank;

import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A Create Fly tank whose insertion/extraction rules are part of the inventory itself. */
public class ConfigurableFluidTank extends FluidTank {
    private Predicate<FluidStack> insertion = stack -> true;
    private Predicate<FluidStack> extraction = stack -> true;
    private final Consumer<FluidStack> updateCallback;

    public ConfigurableFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity);
        this.updateCallback = Objects.requireNonNull(updateCallback);
    }

    public ConfigurableFluidTank allowInsertion(Predicate<FluidStack> predicate) {
        insertion = Objects.requireNonNull(predicate);
        return this;
    }

    public ConfigurableFluidTank forbidInsertion() {
        insertion = stack -> false;
        return this;
    }

    public ConfigurableFluidTank allowExtraction(Predicate<FluidStack> predicate) {
        extraction = Objects.requireNonNull(predicate);
        return this;
    }

    public ConfigurableFluidTank forbidExtraction() {
        extraction = stack -> false;
        return this;
    }

    /** Returns capacity available to a machine-owned insertion, ignoring sided/external insertion rules. */
    public int countInternalSpace(FluidStack stack) {
        if (stack.isEmpty())
            return 0;
        FluidStack stored = getFluid();
        if (!stored.isEmpty() && !FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(stored, stack))
            return 0;
        int storedAmount = stored.isEmpty() ? 0 : stored.getAmount();
        return Math.max(0, Math.min(stack.getAmount(), getMaxAmount(stack) - storedAmount));
    }

    /**
     * Inserts fluid on behalf of the owning machine. This is the Fabric
     * equivalent of NeoForge's internal-tank fill flag and deliberately
     * bypasses only the external insertion predicate.
     */
    public int insertInternal(FluidStack stack) {
        int inserted = countInternalSpace(stack);
        if (inserted == 0)
            return 0;
        FluidStack stored = getFluid();
        FluidStack updated = stored.isEmpty() ? stack.directCopy(inserted) : stored.copy();
        if (!stored.isEmpty())
            updated.setAmount(stored.getAmount() + inserted);
        setFluid(updated);
        return inserted;
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
