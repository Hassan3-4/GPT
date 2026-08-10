/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

@FieldsNullabilityUnknownByDefault
public abstract class BlazeExperienceBlockEntity extends BlazeBlockEntity {
    public static final String LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY = "ExperienceCharge";
    private static final Set<net.minecraft.world.entity.LightningBolt> EXPERIENCE_LIGHTNING =
            Collections.newSetFromMap(new WeakHashMap<>());
    public static final TagKey<Block> LIGHTNING_ROD_BLOCKS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath("c", "lightning_rods"));
    public static final TagKey<PoiType> LIGHTNING_ROD_POINT_OF_INTEREST_TYPES = TagKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            Identifier.fromNamespaceAndPath("c", "lightning_rods"));
    private boolean isCreative;
    protected FluidTankBehaviour tanks;

    public BlazeExperienceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback);

    protected abstract ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tanks = new FluidTankBehaviour(this, List.of(this::createNormalTank, this::createSpecialTank), false);
        behaviours.add(tanks);
    }

    @Override
    public boolean isCreative() {
        return isCreative;
    }

    @Override
    public HeatLevel getHeatLevel() {
        if (getSpecialExperience() > 0)
            return HeatLevel.SEETHING;
        int experienceUnits = getNormalTank().getFluid().getAmount();
        if (experienceUnits > 0) {
            boolean lowPercent = experienceUnits / (double) getNormalTank().getMaxAmountPerStack() < 0.0125;
            return lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
        }
        return HeatLevel.SMOULDERING;
    }

    @Override
    public void write(net.minecraft.world.level.storage.ValueOutput view, boolean clientPacket) {
        view.putBoolean("isCreative", isCreative);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(net.minecraft.world.level.storage.ValueInput view, boolean clientPacket) {
        isCreative = view.getBooleanOr("isCreative", false);
        super.read(view, clientPacket);
        if (isCreative)
            setCreativeTanks(getHeatLevelFromBlock());
    }

    public ConfigurableFluidTank getNormalTank() {
        return tanks.getHandlers()[0];
    }

    public ConfigurableFluidTank getSpecialTank() {
        return tanks.getHandlers()[1];
    }

    public int getNormalExperience() {
        return FluidUnits.toMillibuckets(getNormalTank().getFluid().getAmount());
    }

    public int getSpecialExperience() {
        return FluidUnits.toMillibuckets(getSpecialTank().getFluid().getAmount());
    }

    public int getTotalExperience() {
        return getNormalExperience() + getSpecialExperience();
    }

    public boolean consumeExperience(int amount, boolean special, boolean simulate) {
        int fluidAmount = FluidUnits.fromMillibuckets(amount);
        var fluid = new FluidStack(CEIFluids.EXPERIENCE, fluidAmount);
        var tank = special ? getSpecialTank() : tanks.getCapability();
        if (tank.count(fluid) != fluidAmount)
            return false;
        if (!simulate)
            tank.extract(fluid);
        return true;
    }

    public boolean applyExperienceFuel(ExperienceFuel fuel, boolean forceOverflow, boolean simulate) {
        assert level != null;
        if (isCreative)
            return false;
        boolean special = fuel.special();
        var tank = special ? getSpecialTank() : getNormalTank();
        if (!(tank instanceof ConfigurableFluidTank configurableTank)) {
            return false;
        }
        var fluid = configurableTank.getFluid();
        if (!fluid.isEmpty() && !CEIFluids.EXPERIENCE.isSame(fluid.getFluid()))
            return false;
        int experience = fuel.experience();
        int experienceUnits = FluidUnits.fromMillibuckets(experience);
        var experienceFluid = new FluidStack(CEIFluids.EXPERIENCE, experienceUnits);
        int fill = configurableTank.countInternalSpace(experienceFluid);
        if (fill == 0)
            return false;
        else if (fill != experienceUnits && !forceOverflow)
            return false;
        if (simulate)
            return true;
        if (level.isClientSide())
            spawnParticleBurst(special);
        configurableTank.insertInternal(experienceFluid);

        HeatLevel heat = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (heat != getHeatLevelFromBlock())
            level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.getRandom().nextFloat() * .125f,
                    1.15f - level.getRandom().nextFloat() * .25f);
        notifyUpdate();
        return true;
    }

    public void applyCreativeFuel() {
        assert level != null;
        isCreative = true;
        HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();
        if (level.isClientSide()) {
            spawnParticleBurst(next.isAtLeast(HeatLevel.SEETHING));
            return;
        }
        playSound();
        if (next == HeatLevel.FADING)
            next = next.nextActiveLevel();
        setCreativeTanks(next);
        setBlockHeat(next);
        notifyUpdate();
    }

    protected void setCreativeTanks(HeatLevel heatLevel) {
        switch (heatLevel) {
            case KINDLED -> {
                int capacity = getNormalTank().getMaxAmountPerStack();
                tanks.setTank(0, callback -> new CreativeConfigurableFluidTank(capacity, callback));
                getNormalTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE, capacity));
            }
            case SEETHING -> {
                int capacity = getSpecialTank().getMaxAmountPerStack();
                tanks.setTank(1, callback -> new CreativeConfigurableFluidTank(capacity, callback));
                getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE, capacity));
            }
            default -> {
                tanks.setTank(0, this::createNormalTank);
                tanks.setTank(1, this::createSpecialTank);
            }
        }
    }

    protected @Nullable BlockPos getStrikePos() {
        assert level != null;
        var dimension = level.dimensionType();
        if (!dimension.hasSkyLight())
            return null;
        if (dimension.hasCeiling())
            return null;
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, worldPosition).below();
    }

    @SuppressWarnings("all")
    protected boolean strikeLightning(ServerLevel level, BlockPos strikePos) {
        var lightning = EntityTypes.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (lightning == null)
            return false;
        EXPERIENCE_LIGHTNING.add(lightning);
        Optional<BlockPos> rodPos = level.getPoiManager().findAll(
                poi -> poi.is(LIGHTNING_ROD_POINT_OF_INTEREST_TYPES),
                pos -> pos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1,
                strikePos,
                128,
                PoiManager.Occupancy.ANY).unordered().findAny();
        lightning.snapTo(Vec3.atBottomCenterOf(rodPos.orElse(strikePos).above()));
        level.addFreshEntity(lightning);
        return rodPos.isEmpty();
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);
        boolean special = false;
        for (var tank : tanks.getHandlers()) {
            CEILang.translate(special
                            ? "gui.goggles.blaze_experience.super_experience"
                            : "gui.goggles.blaze_experience.experience")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
            CreateLang.builder()
                    .add(CreateLang.number(FluidUnits.toMillibuckets(tank.getFluid().getAmount()))
                            .add(mb)
                            .style(special ? ChatFormatting.BLUE : ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(FluidUnits.toMillibuckets(tank.getMaxAmountPerStack()))
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 2);
            special = true;
        }
        return true;
    }

    private static final class CreativeConfigurableFluidTank extends ConfigurableFluidTank {
        private CreativeConfigurableFluidTank(int capacity, Consumer<FluidStack> callback) {
            super(capacity, callback);
        }

        @Override
        public int extract(FluidStack stack, int maxAmount) {
            return CEIFluids.EXPERIENCE.isSame(stack.getFluid()) ? maxAmount : 0;
        }

        @Override
        public boolean preciseExtract(FluidStack stack) {
            return CEIFluids.EXPERIENCE.isSame(stack.getFluid());
        }

        @Override
        public void setFluid(FluidStack stack) {
            super.setFluid(stack);
            if (!stack.isEmpty()) stack.setAmount(getMaxAmountPerStack());
        }
    }

    /** Fabric entity data has no NeoForge persistent-data bag; the marker is
     * only needed for this short-lived lightning entity and is tracked safely. */
    public static boolean isExperienceLightning(net.minecraft.world.entity.LightningBolt lightningBolt) {
        return EXPERIENCE_LIGHTNING.contains(lightningBolt);
    }
}
