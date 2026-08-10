/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

/** Server-side filter and amount logic for the Experience Hatch.
 * Client value-box rendering is registered separately, keeping client classes
 * out of the dedicated-server block entity path. */
public final class ExperienceHatchBehaviour extends ServerFilteringBehaviour {
    /**
     * Share Create Fly's base filtering type so its client filtering renderer
     * can resolve this custom server behaviour after the hatch is placed.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BehaviourType<ExperienceHatchBehaviour> TYPE =
        (BehaviourType) ServerFilteringBehaviour.TYPE;
    public static final int POINTS_PER_SCROLL = 10;

    public ExperienceHatchBehaviour(SmartBlockEntity blockEntity) {
        super(blockEntity);
        forFluids();
        count = 0;
    }

    public FluidStack getFluidToDrain() {
        Holder<Fluid> fluid = selectedFluid();
        int unit = unitOf(fluid);
        if (unit == 0) return FluidStack.EMPTY;
        int amount = count == 0 ? Integer.MAX_VALUE : count * POINTS_PER_SCROLL * unit;
        return new FluidStack(fluid.value(), amount);
    }

    public FluidStack getFluidToFill(int available) {
        if (available == 0) return FluidStack.EMPTY;
        Holder<Fluid> fluid = selectedFluid();
        int unit = unitOf(fluid);
        if (unit == 0) return FluidStack.EMPTY;
        int availableFluid = ExperienceHelper.getFluidFromExperience(fluid, available);
        int amount = count == 0
            ? availableFluid
            : Math.min(availableFluid, count * POINTS_PER_SCROLL * unit);
        return new FluidStack(fluid.value(), amount);
    }

    private Holder<Fluid> selectedFluid() {
        FluidStack selected = filter.fluid(getLevel());
        if (selected.isEmpty() || Fluids.EMPTY.isSame(selected.getFluid())) {
            return CEIFluids.EXPERIENCE.builtInRegistryHolder();
        }
        return selected.getRegistryEntry();
    }

    private static int unitOf(Holder<Fluid> fluid) {
        return ExperienceHelper.getExperienceFluidUnit(fluid);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("ExperienceScroll", count);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        count = view.getIntOr("ExperienceScroll", count);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings)) return;
        count = Math.max(0, settings.value());
        blockEntity.setChanged();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, count);
    }

    @Override
    public boolean isCountVisible() {
        return true;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        FilterItemStack candidate = FilterItemStack.of(stack.copy());
        if (!candidate.isEmpty()) {
            FluidStack fluid = candidate.fluid(getLevel());
            if (!fluid.isEmpty() && !CEIFluids.EXPERIENCE.isSame(fluid.getFluid())
                && CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid.getRegistryEntry()) == null) {
                return false;
            }
        }
        return super.setFilter(stack);
    }

    @Override
    public String getClipboardKey() {
        return "ExperienceHatch";
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
