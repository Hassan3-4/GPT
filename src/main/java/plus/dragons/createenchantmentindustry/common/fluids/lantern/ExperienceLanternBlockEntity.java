/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import static net.minecraft.world.level.block.DirectionalBlock.FACING;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Stores nearby player/orb experience as CEI fluid and exposes it from the
 * lantern's back face through Create Fly's native fluid inventory API. */
public class ExperienceLanternBlockEntity extends SmartBlockEntity {
    protected FluidTankBehaviour tank;
    protected final AABB effectiveAABB;
    protected final int rate;

    public ExperienceLanternBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        effectiveAABB = new AABB(pos).inflate(.5);
        rate = CEIConfig.fluids().experienceLanternDrainRate.get();
    }

    protected ConfigurableFluidTank createTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(
            FluidUnits.fromMillibuckets(CEIConfig.fluids().experienceLanternFluidCapacity.get()),
            fluidUpdateCallback.andThen(this::onFluidStackChanged))
            .allowInsertion(stack -> CEIFluids.EXPERIENCE.isSame(stack.getFluid()));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = new FluidTankBehaviour(this, List.of(this::createTank), false);
        behaviours.add(tank);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && level.getGameTime() % 10 == 0) drainExp();
        if (!level.isClientSide() && CEIConfig.fluids().experienceLanternPullToggle.get()) pullExp();
    }

    public FluidTankBehaviour getTank() { return tank; }

    protected void drainExp() {
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveAABB,
            player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger wanted = new AtomicInteger();
            for (Player player : players) {
                int experience = ExperienceHelper.getExperienceForPlayer(player);
                wanted.addAndGet(Math.min(experience, rate));
            }
            int insertedUnits = tank.getPrimaryHandler().insert(new FluidStack(
                CEIFluids.EXPERIENCE,
                FluidUnits.fromMillibuckets(wanted.get())
            ));
            int inserted = FluidUnits.toMillibuckets(insertedUnits);
            for (Player player : players) {
                if (inserted == 0) break;
                int removed = Math.min(Math.min(ExperienceHelper.getExperienceForPlayer(player), rate), inserted);
                if (removed != 0) {
                    player.giveExperiencePoints(-removed);
                    inserted -= removed;
                }
            }
        }

        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB)) {
            int amount = orb.getValue();
            int insertedUnits = tank.getPrimaryHandler().insert(new FluidStack(
                CEIFluids.EXPERIENCE,
                FluidUnits.fromMillibuckets(amount)
            ));
            int inserted = FluidUnits.toMillibuckets(insertedUnits);
            if (inserted == amount) {
                orb.remove(Entity.RemovalReason.DISCARDED);
            } else {
                if (inserted != 0) {
                    orb.discard();
                    level.addFreshEntity(new ExperienceOrb(level, orb.getX(), orb.getY(), orb.getZ(), amount - inserted));
                }
                break;
            }
        }
    }

    protected void pullExp() {
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class,
                effectiveAABB.inflate(CEIConfig.fluids().experienceLanternPullRadius.get()))) {
            if (orb.getDeltaMovement().length() > .5) continue;
            double distance = orb.position().distanceTo(worldPosition.getCenter());
            if (distance < .001) continue;
            double force = CEIConfig.fluids().experienceLanternPullForceMultiplier.get() / distance;
            var direction = worldPosition.getCenter().subtract(orb.position()).normalize().scale(force);
            orb.push(direction);
        }
    }

    protected void onFluidStackChanged(FluidStack ignored) {
        if (level == null) return;
        int capacity = tank.getPrimaryHandler().getMaxAmountPerStack();
        int amount = tank.getPrimaryHandler().getFluid().getAmount();
        int light = Math.clamp((int) ((amount / (float) capacity) * 15), 0, 15);
        BlockState state = getBlockState();
        if (state.getValue(ExperienceLanternBlock.LIGHT) != light) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ExperienceLanternBlock.LIGHT, light));
        }
    }

    public @Nullable FluidInventory getFluidHandler(@Nullable Direction side) {
        return side == null || side.getOpposite() == getBlockState().getValue(FACING) ? tank.getCapability() : null;
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }
}
