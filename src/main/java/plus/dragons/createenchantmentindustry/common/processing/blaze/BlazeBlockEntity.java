/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.blaze;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Shared live-blaze state, heat updates, particles and client animation state. */
public abstract class BlazeBlockEntity extends SmartBlockEntity {
    public final LerpedFloat headAnimation = LerpedFloat.linear();
    public final LerpedFloat headAngle = LerpedFloat.angular();

    protected BlazeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isActive();

    public abstract boolean isCreative();

    public abstract HeatLevel getHeatLevel();

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            if (shouldTickAnimation()) {
                tickAnimation();
            }
            if (!isVirtual()) {
                spawnParticles(getHeatLevelFromBlock());
            }
            return;
        }
        if (!isCreative()) {
            updateBlockState();
        }
    }

    @Environment(EnvType.CLIENT)
    protected @Nullable PartialModel getGogglesModel(HeatLevel heatLevel) {
        return null;
    }

    @Environment(EnvType.CLIENT)
    protected @Nullable PartialModel getHatModel(HeatLevel heatLevel) {
        return null;
    }

    @Environment(EnvType.CLIENT)
    public final @Nullable PartialModel getGogglesModelForRender(HeatLevel heatLevel) {
        return getGogglesModel(heatLevel);
    }

    @Environment(EnvType.CLIENT)
    public final @Nullable PartialModel getHatModelForRender(HeatLevel heatLevel) {
        return getHatModel(heatLevel);
    }

    @Environment(EnvType.CLIENT)
    protected boolean shouldTickAnimation() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    protected void tickAnimation() {
        boolean active = getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && isActive();
        if (active) {
            headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(BlazeBurnerBlock.FACING)
                .orElse(Direction.SOUTH)) + 180) % 360, .125f, Chaser.EXP);
            headAngle.tickChaser();
        } else {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x = isVirtual() ? -4 : player.getX();
                double z = isVirtual() ? -10 : player.getZ();
                target = AngleHelper.deg(-Mth.atan2(z - (getBlockPos().getZ() + .5), x - (getBlockPos().getX() + .5))) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, Chaser.exp(5));
            headAngle.tickChaser();
        }

        headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        headAnimation.tickChaser();
    }

    public HeatLevel getHeatLevelFromBlock() {
        return BlazeBlock.getHeatLevelOf(getBlockState());
    }

    public HeatLevel getHeatLevelForRender() {
        HeatLevel heatLevel = getHeatLevelFromBlock();
        return heatLevel.isAtLeast(HeatLevel.FADING) ? heatLevel : HeatLevel.SMOULDERING;
    }

    public void updateBlockState() {
        setBlockHeat(getHeatLevel());
    }

    protected void onHeatChange(HeatLevel currentHeat, HeatLevel newHeat) {
    }

    protected void setBlockHeat(HeatLevel newHeat) {
        HeatLevel currentHeat = getHeatLevelFromBlock();
        if (currentHeat == newHeat || level == null) {
            return;
        }
        onHeatChange(currentHeat, newHeat);
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlazeBlock.HEAT_LEVEL, newHeat));
        notifyUpdate();
    }

    protected void playSound() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS,
                .125f + level.random.nextFloat() * .125f, .75f - level.random.nextFloat() * .25f);
        }
    }

    protected void spawnParticles(HeatLevel heatLevel) {
        if (level == null || heatLevel == HeatLevel.NONE || level.random.nextInt(4) != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        Vec3 smokePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, .125f).multiply(1, 0, 1));
        boolean empty = level.getBlockState(worldPosition.above()).getCollisionShape(level, worldPosition.above()).isEmpty();
        if (empty || random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, smokePos.x, smokePos.y, smokePos.z, 0, 0, 0);
        }
        double yMotion = empty ? .0625f : random.nextDouble() * .0125f;
        Vec3 flamePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, .5f).multiply(1, .25f, 1).normalize()
            .scale((empty ? .25 : .5) + random.nextDouble() * .125)).add(0, .5, 0);
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        }
    }

    protected void spawnParticleBurst(boolean soul) {
        if (level == null) {
            return;
        }
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        RandomSource random = level.random;
        for (int i = 0; i < 20; i++) {
            Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, random, .5f).multiply(1, .25f, 1).normalize();
            Vec3 pos = center.add(offset.scale(.5 + random.nextDouble() * .125)).add(0, .125, 0);
            Vec3 motion = offset.scale(1 / 32f);
            level.addParticle(soul ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }
}
