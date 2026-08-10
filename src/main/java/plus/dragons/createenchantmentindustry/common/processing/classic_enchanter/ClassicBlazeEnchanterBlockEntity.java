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

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import java.util.List;
import java.util.function.Consumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancements.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ClassicBlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    protected static final int ENCHANTING_TIME = 200;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected int processingTime = -1;
    protected boolean special;
    protected boolean cursed;
    protected ClassicEnchanterBehaviour enchanter;
    protected AdvancementBehaviour advancement;
    float flip;
    float oFlip;
    float flipT;
    float flipA;

    public ClassicBlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public LerpedFloat headAngle() {
        return this.headAngle;
    }

    public @Nullable FluidInventory getFluidHandler(@Nullable Direction side) {
        if ((side == Direction.DOWN || side == null) && !isRemoved())
            return tanks.getCapability();
        return null;
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(
                FluidUnits.fromMillibuckets(CEIConfig.processing().classicBlazeEnchanterFluidCapacity.get()),
                fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.isOf(CEIFluids.EXPERIENCE));
    }

    @Override
    protected ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(
                FluidUnits.fromMillibuckets(CEIConfig.processing().classicBlazeEnchanterFluidCapacity.get()),
                fluidUpdateCallback)
                .forbidInsertion();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        enchanter = new ClassicEnchanterBehaviour(this);
        advancement = new AdvancementBehaviour(this);
        behaviours.add(enchanter);
        behaviours.add(advancement);
        behaviours.add(new DirectBeltInputBehaviour(this)
                .onlyInsertWhen(side -> heldItem.isEmpty())
                .setInsertionHandler(((transportedItemStack, side, simulate) -> this.insertItem(transportedItemStack.stack, simulate))));
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null)
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return stack;
        if (!heldItem.isEmpty())
            return stack;
        var input = stack.copy();
        var inserted = input.split(1);
        if (!enchanter.canProcess(inserted)) {
            return stack;
        }
        if (simulate)
            return input;
        heldItem = inserted;
        notifyUpdate();
        return input;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        assert level != null;
        ItemStack extracted = ItemStack.EMPTY;
        if (forced || processingTime <= 0) {
            extracted = heldItem.copy();
            if (!simulate) {
                heldItem = ItemStack.EMPTY;
                processingTime = -1;
                notifyUpdate();
            }
        }
        return extracted;
    }

    @Override
    public void tick() {
        super.tick();
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return;
        boolean special = getHeatLevelFromBlock() == BlazeBurnerBlock.HeatLevel.SEETHING;
        if (this.special != special) {
            this.special = special;
        }
        var strikePos = getStrikePos();
        boolean cursed = special && !worldPosition.equals(strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
        }
        bookTick();
        if (heldItem.isEmpty()) return;
        if (level.isClientSide() && isVirtual()) {
            if (enchanter.canProcess(heldItem)) {
                var cost = enchanter.getExperienceCost(heldItem);
                if (processingTime < 0) {
                    processingTime = ENCHANTING_TIME / 4;
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    return;
                }
                processingTime = -1;
                heldItem = enchanter.getResult(heldItem);
                advancement.awardStat(CEIStats.CLASSIC_ENCHANT.get(), 1);
                consumeExperience(cost, special, false);
                return;
            }
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (enchanter.canProcess(heldItem)) {
            var cost = enchanter.getExperienceCost(heldItem);
            if (cost > 0 && consumeExperience(cost, special, true)) {
                if (processingTime < 0) {
                    processingTime = ENCHANTING_TIME;
                    notifyUpdate();
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    notifyUpdate();
                    return;
                }
                if (special && !cursed && strikePos != null && strikeLightning(serverLevel, strikePos)) {
                    serverLevel.destroyBlock(worldPosition, false);
                    serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.defaultBlockState());
                    return;
                }
                processingTime = -1;
                heldItem = enchanter.getResult(heldItem);
                advancement.awardStat(CEIStats.CLASSIC_ENCHANT.get(), 1);
                consumeExperience(cost, special, false);
                notifyUpdate();
                level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                spawnEnchantParticles();
            } else {
                if (processingTime != -1) {
                    processingTime = -1;
                    notifyUpdate();
                }
            }
        } else {
            if (processingTime != -1) {
                processingTime = -1;
                notifyUpdate();
            }
            tryExport();
        }
    }

    protected void tryExport() {
        for (var side : Direction.Plane.HORIZONTAL) {
            BlockPos nextPosition = worldPosition.relative(side);
            DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
            if (directBeltInputBehaviour != null && directBeltInputBehaviour.canInsertFromSide(side)) {
                ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);
                if (returned.isEmpty()) {
                    heldItem = ItemStack.EMPTY;
                    notifyUpdate();
                    return;
                } else if (returned.getCount() != heldItem.getCount()) {
                    heldItem = returned.copy();
                    notifyUpdate();
                    return;
                }
            }
        }
    }

    @Override
    public @Nullable PartialModel getGogglesModel(BlazeBurnerBlock.HeatLevel heatLevel) {
        return super.getGogglesModel(heatLevel);
    }

    @Override
    public void tickAnimation() {
        super.tickAnimation();
    }

    protected void bookTick() {
        if (level.getRandom().nextInt(40) == 0) {
            float oFlipT = flipT;
            while (oFlipT == flipT) {
                flipT += (level.getRandom().nextInt(4) - level.getRandom().nextInt(4));
            }
        }
        oFlip = flip;
        float flipDiff = (flipT - flip) * 0.4F;
        flipDiff = Mth.clamp(flipDiff, -0.2F, 0.2F);
        flipA += (flipDiff - flipA) * 0.9F;
        flip += flipA;
    }

    protected void spawnEnchantParticles() {
        if (isVirtual())
            return;
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        vec = vec.add(0, 1, 0);
        ParticleOptions particle = ParticleTypes.ENCHANT;
        for (int i = 0; i < 20; i++) {
            Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), 1f);
            m = new Vec3(m.x, Math.abs(m.y), m.z);
            level.addAlwaysVisibleParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
        level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, level.getRandom().nextFloat() * .1f + .9f, true);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= enchanter.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("ProcessingTime", processingTime);
        view.store("HeldItem", ItemStack.OPTIONAL_CODEC, heldItem);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        processingTime = view.getIntOr("ProcessingTime", -1);
        heldItem = view.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    public LerpedFloat headAnimation() {
        return this.headAnimation;
    }

    public ClassicEnchanterBehaviour getEnchanterBehaviour() {
        return enchanter;
    }

    @Override
    public void clearContent() {
        heldItem = ItemStack.EMPTY;
    }

    public static class EnchanterTransform extends ValueBoxTransform.Sided {
        public EnchanterTransform() {}

        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace((double) 8.0F, (double) 8.0F, (double) 13.5F);
        }

        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
