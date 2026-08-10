package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancements.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

/** The item-and-fluid input beneath a mechanical grindstone. */
public class GrindstoneDrainBlock extends HorizontalKineticBlock implements IBE<GrindstoneDrainBlockEntity>,
    SpecialBlockItemRequirement, ItemInventoryProvider<GrindstoneDrainBlockEntity>, FluidInventoryProvider<GrindstoneDrainBlockEntity> {
    protected static final VoxelShape SHAPE = new AllShapes.Builder(AllShapes.CASING_13PX.get(Direction.UP))
        .add(3, 3, 3, 13, 13, 13).build();
    final MechanicalGrindstoneBlock grindstone;

    public GrindstoneDrainBlock(MechanicalGrindstoneBlock grindstone, Properties properties) {
        super(properties);
        this.grindstone = grindstone;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP) return grindstone.useItemOn(stack, state, level, pos, player, hand, hitResult);
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP)
            return grindstone.useWithoutItem(state, level, pos, player, hitResult);
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter world, Entity entity) {
        super.updateEntityMovementAfterFallOn(world, entity);
        if (!(entity instanceof ItemEntity item) || !entity.isAlive() || entity.level().isClientSide()) return;
        GrindstoneDrainBlockEntity drain = getBlockEntity(world, entity.blockPosition());
        if (drain == null || !drain.inventory.canPlaceItem(0, item.getItem())) return;
        ItemStack incoming = item.getItem();
        ItemStack existing = drain.inventory.getItem(0);
        if (!existing.isEmpty()) return;
        ItemStack inserted = incoming.copyWithCount(1);
        drain.inventory.setItem(0, inserted);
        incoming.shrink(1);
        if (incoming.isEmpty()) item.discard(); else item.setItem(incoming);
        drain.setChanged();
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, GrindstoneDrainBlockEntity blockEntity,
                                  Direction context) {
        return blockEntity.getItemHandler(context);
    }

    @Override
    public FluidInventory getFluidInventory(LevelAccessor world, BlockPos pos, BlockState state,
                                             GrindstoneDrainBlockEntity blockEntity, Direction context) {
        return blockEntity.getFluidHandler(context);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return state.getValue(HORIZONTAL_FACING) == face; }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    public @Nullable Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
        Direction preferred = super.getPreferredHorizontalFacing(context);
        if (preferred != null) return preferred;
        for (Direction facing : Iterate.horizontalDirections) {
            BlockPos adjacent = context.getClickedPos().relative(facing);
            if (!FluidPipeBlock.canConnectTo(context.getLevel(), adjacent, context.getLevel().getBlockState(adjacent), facing)) continue;
            if (preferred != null && preferred.getAxis() != facing.getAxis()) return null;
            preferred = facing;
        }
        return preferred == null ? null : preferred.getOpposite();
    }

    @Override protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) { return new ItemStack(AllBlocks.ITEM_DRAIN); }
    @Override public Axis getRotationAxis(BlockState state) { return state.getValue(HORIZONTAL_FACING).getAxis(); }
    @Override public Class<GrindstoneDrainBlockEntity> getBlockEntityClass() { return GrindstoneDrainBlockEntity.class; }
    @Override public BlockEntityType<? extends GrindstoneDrainBlockEntity> getBlockEntityType() { return CEIBlockEntities.GRINDSTONE_DRAIN.get(); }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(List.of(
            new ItemRequirement.StackRequirement(new ItemStack(grindstone), ItemRequirement.ItemUseType.CONSUME),
            new ItemRequirement.StackRequirement(new ItemStack(AllBlocks.ITEM_DRAIN), ItemRequirement.ItemUseType.CONSUME)
        ));
    }
}
