/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlock;

/** Common insertion and player-interaction path for CEI's blaze machines. */
public abstract class BlazeExperienceBlock<T extends BlazeExperienceBlockEntity> extends BlazeBlock<T> {
    protected BlazeExperienceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        T blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        FuelApplication application = applyFuel(state, level, pos, stack, !FakePlayerHandler.has(player), player.isCreative(), false);
        if (application.result() == InteractionResult.PASS) {
            return InteractionResult.PASS;
        }
        if (application.result() == InteractionResult.FAIL) {
            return InteractionResult.FAIL;
        }
        ItemStack remainder = application.remainder();
        if (!level.isClientSide() && !remainder.isEmpty()) {
            if (stack.isEmpty()) player.setItemInHand(hand, remainder);
            else if (!player.getInventory().add(remainder)) player.drop(remainder, false);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    /** Applies an experience fuel while retaining the remainder used by arm automation. */
    public static FuelApplication applyFuel(BlockState state, Level level, BlockPos pos, ItemStack stack,
                                            boolean forceOverflow, boolean doNotConsume, boolean simulate) {
        if (!state.hasBlockEntity()) return new FuelApplication(InteractionResult.FAIL, ItemStack.EMPTY);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlazeExperienceBlockEntity blaze)) {
            return new FuelApplication(InteractionResult.FAIL, ItemStack.EMPTY);
        }
        if (stack.is(AllItems.CREATIVE_BLAZE_CAKE)) {
            if (!simulate) {
                blaze.applyCreativeFuel();
                if (!doNotConsume && !level.isClientSide()) stack.shrink(1);
            }
            return new FuelApplication(InteractionResult.SUCCESS, ItemStack.EMPTY);
        }
        ExperienceFuel fuel = ExperienceFuel.get(level, stack);
        if (fuel == null) return new FuelApplication(InteractionResult.PASS, ItemStack.EMPTY);
        if (!blaze.applyExperienceFuel(fuel, forceOverflow, simulate)) {
            return new FuelApplication(InteractionResult.FAIL, ItemStack.EMPTY);
        }
        if (!doNotConsume && !simulate && !level.isClientSide()) stack.shrink(1);
        ItemStack remainder = doNotConsume ? ItemStack.EMPTY : fuel.usingConvertTo()
            .orElse(stack.getItem().getCraftingRemainder()).copy();
        return new FuelApplication(InteractionResult.SUCCESS, remainder);
    }

    public record FuelApplication(InteractionResult result, ItemStack remainder) {
    }
}
