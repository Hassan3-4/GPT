/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.fluids.hatch;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;

/**
 * Fluid hatch backed by Create Fly's Fabric fluid inventories.  The hatch uses
 * the same generic item and recipe transfer paths as Create Fly, while all
 * fluid mutations pass through the target inventory so transactions, filters
 * and storage-side limits are preserved.
 */
public class FluidHatchBlock extends HorizontalDirectionalBlock implements IBE<FluidHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<FluidHatchBlock> CODEC = simpleCodec(FluidHatchBlock::new);

    public FluidHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || context.getClickedFace().getAxis().isVertical()) {
            return null;
        }
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        updateWater(level, ticks, state, pos);
        return state;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || FakePlayerHandler.has(player)) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity target = level.getBlockEntity(pos.relative(state.getValue(FACING)));
        if (target == null) {
            return InteractionResult.FAIL;
        }
        FluidInventory inventory = AllTransfer.getFluidInventory(level, target.getBlockPos(), target.getBlockState(), target, null);
        if (inventory == null) {
            return InteractionResult.FAIL;
        }
        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, ServerFilteringBehaviour.TYPE);
        if (filter == null) {
            return InteractionResult.FAIL;
        }

        FluidStack transferred;
        boolean filling = player.isSecondaryUseActive();
        if (filling) {
            transferred = fillItem(level, player, hand, stack, target, inventory, filter);
            if (transferred.isEmpty()) {
                transferred = emptyItem(level, player, hand, stack, target, inventory, filter);
                filling = false;
            }
        } else {
            transferred = emptyItem(level, player, hand, stack, target, inventory, filter);
            if (transferred.isEmpty()) {
                transferred = fillItem(level, player, hand, stack, target, inventory, filter);
                filling = true;
            }
        }

        if (transferred.isEmpty()) {
            return GenericItemEmptying.canItemBeEmptied(level, stack)
                    || GenericItemFilling.canItemBeFilled(level, stack)
                    || FluidHatchFillingRecipeTransfer.canItemBeFilled(level, stack)
                    ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        playTransferSound(level, pos, transferred, filling);
        return InteractionResult.SUCCESS;
    }

    private static FluidStack emptyItem(
            Level level, Player player, InteractionHand hand, ItemStack stack, BlockEntity target,
            FluidInventory inventory, ServerFilteringBehaviour filter) {
        if (!GenericItemEmptying.canItemBeEmptied(level, stack)) {
            return FluidStack.EMPTY;
        }
        ItemStack working = stack.copy();
        var simulated = GenericItemEmptying.emptyItem(level, working, true);
        FluidStack fluid = simulated.getFirst();
        if (fluid.isEmpty() || !filter.test(fluid) || !inventory.forcePreciseInsert(fluid.copy())) {
            return FluidStack.EMPTY;
        }

        working = stack.copy();
        var emptied = GenericItemEmptying.emptyItem(level, working, false);
        if (emptied.getFirst().isEmpty()) {
            inventory.extract(fluid);
            return FluidStack.EMPTY;
        }
        target.setChanged();
        notifyTarget(level, target);
        if (!player.isCreative()) {
            replaceItem(player, hand, working, emptied.getSecond());
        }
        return fluid;
    }

    private static FluidStack fillItem(
            Level level, Player player, InteractionHand hand, ItemStack stack, BlockEntity target,
            FluidInventory inventory, ServerFilteringBehaviour filter) {
        for (FluidStack stored : inventory) {
            if (stored.isEmpty() || !filter.test(stored)) {
                continue;
            }

            FluidStack available = stored.copy();
            var recipeAmount = FluidHatchFillingRecipeTransfer.getRequiredAmountForItem(level, stack, available);
            if (recipeAmount.isPresent()) {
                int amount = recipeAmount.getAsInt();
                if (amount > 0 && amount <= available.getAmount()) {
                    ItemStack working = stack.copy();
                    var result = FluidHatchFillingRecipeTransfer.fillItem(level, amount, working, available.copy());
                    FluidStack moved = available.copyWithAmount(amount);
                    if (result.isPresent() && inventory.preciseExtract(moved)) {
                        target.setChanged();
                        notifyTarget(level, target);
                        if (!player.isCreative()) {
                            replaceItem(player, hand, working, result.get());
                        }
                        return moved;
                    }
                }
            }

            int amount = FluidHatchItemFilling.getRequiredAmountForItem(level, stack, available);
            if (amount <= 0 || amount > available.getAmount()) {
                continue;
            }
            ItemStack working = stack.copy();
            ItemStack result = FluidHatchItemFilling.fillItem(level, amount, working, available.copy());
            FluidStack moved = available.copyWithAmount(amount);
            if (result.isEmpty() || !inventory.preciseExtract(moved)) {
                continue;
            }
            target.setChanged();
            notifyTarget(level, target);
            if (!player.isCreative()) {
                replaceItem(player, hand, working, result);
            }
            return moved;
        }
        return FluidStack.EMPTY;
    }

    private static void replaceItem(Player player, InteractionHand hand, ItemStack working, ItemStack result) {
        if (working.isEmpty()) {
            player.setItemInHand(hand, result);
        } else {
            player.setItemInHand(hand, working);
            player.getInventory().placeItemBackInInventory(result);
        }
    }

    private static void notifyTarget(Level level, BlockEntity target) {
        if (level instanceof ServerLevel server) {
            server.getChunkSource().blockChanged(target.getBlockPos());
        }
    }

    private static void playTransferSound(Level level, BlockPos pos, FluidStack fluid, boolean tankToItem) {
        SoundEvent sound = tankToItem ? FluidHelper.getFillSound(fluid) : FluidHelper.getEmptySound(fluid);
        float pitch = Mth.clamp(1 - fluid.getAmount() / 16000F, 0, 1) / 1.5F + .5F
                + (level.random.nextFloat() - .5F) / 4F;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, .5F, pitch);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public Class<FluidHatchBlockEntity> getBlockEntityClass() {
        return FluidHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidHatchBlockEntity> getBlockEntityType() {
        return CDPBlockEntities.FLUID_HATCH;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
