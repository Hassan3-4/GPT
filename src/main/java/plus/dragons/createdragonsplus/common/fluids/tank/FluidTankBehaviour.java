/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.fluids.tank;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.fluids.CombinedTankWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** CDP multi-tank behaviour implemented on Create Fly's Fabric fluid inventory API. */
public class FluidTankBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<FluidTankBehaviour> TYPE = new BehaviourType<>();
    private static final int SYNC_RATE = 8;
    private final TankSegment[] tanks;
    private final SidedFluidInventory capability;
    private Runnable updateCallback = () -> { };
    private int syncCooldown;
    private boolean queuedSync;

    public FluidTankBehaviour(SmartBlockEntity blockEntity, List<TankFactory> factories, boolean enforceVariety) {
        super(blockEntity);
        tanks = new TankSegment[factories.size()];
        ConfigurableFluidTank[] handlers = new ConfigurableFluidTank[factories.size()];
        for (int i = 0; i < factories.size(); i++) {
            tanks[i] = new TankSegment(factories.get(i));
            handlers[i] = tanks[i].tank;
        }
        capability = enforceVariety ? new VarietyTankWrapper(handlers) : new CombinedTankWrapper(handlers);
    }

    public FluidTankBehaviour(SmartBlockEntity blockEntity, TankFactory factory) {
        this(blockEntity, List.of(factory), false);
    }

    public FluidTankBehaviour whenFluidUpdates(Runnable callback) {
        updateCallback = callback;
        return this;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!getLevel().isClientSide()) {
            forEach(TankSegment::onFluidChanged);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0 && --syncCooldown == 0 && queuedSync) {
            updateFluids();
        }
        forEach(segment -> segment.level.tickChaser());
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        updateFluids();
    }

    public void sendDataLazily() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        updateFluids();
        syncCooldown = SYNC_RATE;
        queuedSync = false;
    }

    private void updateFluids() {
        updateCallback.run();
        blockEntity.sendData();
        blockEntity.setChanged();
    }

    public ConfigurableFluidTank getPrimaryHandler() {
        return tanks[0].tank;
    }

    public TankSegment getPrimaryTank() {
        return tanks[0];
    }

    public TankSegment[] getTanks() {
        return tanks;
    }

    public FluidInventory getCapability() {
        return capability;
    }

    public void setTank(int index, TankFactory factory) {
        tanks[index] = new TankSegment(factory);
        updateFluids();
    }

    public boolean isEmpty() {
        for (TankSegment tank : tanks) {
            if (!tank.tank.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void forEach(Consumer<TankSegment> action) {
        for (TankSegment tank : tanks) {
            action.accept(tank);
        }
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        ValueOutput.ValueOutputList list = view.childrenList(getType().getName() + "Tanks");
        forEach(segment -> segment.write(list.addChild()));
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        int index = 0;
        for (ValueInput entry : view.childrenListOrEmpty(getType().getName() + "Tanks")) {
            if (index == tanks.length) {
                break;
            }
            tanks[index++].read(entry, clientPacket);
        }
    }

    @FunctionalInterface
    public interface TankFactory {
        ConfigurableFluidTank create(Consumer<FluidStack> updateCallback);
    }

    public final class TankSegment {
        public final ConfigurableFluidTank tank;
        private final LerpedFloat level = LerpedFloat.linear().startWithValue(0).chase(0, .25F, Chaser.EXP);
        private FluidStack rendered = FluidStack.EMPTY;

        private TankSegment(TankFactory factory) {
            tank = factory.create(ignored -> onFluidChanged());
        }

        private void onFluidChanged() {
            if (!blockEntity.hasLevel()) {
                return;
            }
            level.chase(tank.getFluid().getAmount() / (float) tank.getMaxAmountPerStack(), .25F, Chaser.EXP);
            if (!getLevel().isClientSide()) {
                sendDataLazily();
            }
            if (!tank.getFluid().isEmpty()) {
                rendered = tank.getFluid().copy();
            }
        }

        public FluidStack getRenderedFluid() {
            return rendered;
        }

        public LerpedFloat getFluidLevel() {
            return level;
        }

        public float getTotalUnits(float partialTicks) {
            return level.getValue(partialTicks) * tank.getMaxAmountPerStack();
        }

        public boolean isEmpty(float partialTicks) {
            return rendered.isEmpty() || getTotalUnits(partialTicks) < 1;
        }

        private void write(ValueOutput view) {
            view.store("TankContent", FluidStack.OPTIONAL_CODEC, tank.getFluid());
            level.write(view);
        }

        private void read(ValueInput view, boolean clientPacket) {
            tank.setFluid(view.read("TankContent", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
            level.read(view, clientPacket);
            if (!tank.getFluid().isEmpty()) {
                rendered = tank.getFluid().copy();
            }
        }
    }

    private static final class VarietyTankWrapper extends CombinedTankWrapper {
        private VarietyTankWrapper(FluidInventory... inventories) {
            super(inventories);
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction side) {
            if (!super.canInsert(slot, stack, side)) {
                return false;
            }
            FluidStack own = getStack(slot);
            if (!own.isEmpty()) {
                return FluidInventory.matches(own, stack);
            }
            for (int index : slots) {
                if (index != slot) {
                    FluidStack other = getStack(index);
                    if (!other.isEmpty() && FluidInventory.matches(other, stack)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
