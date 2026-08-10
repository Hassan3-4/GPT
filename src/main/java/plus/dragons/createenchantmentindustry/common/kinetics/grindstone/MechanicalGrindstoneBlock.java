package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.common.registry.CEIDamageSources;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;

/** Hand interaction and kinetic presentation for the mechanical grindstone. */
public class MechanicalGrindstoneBlock extends RotatedPillarKineticBlock implements IBE<KineticBlockEntity> {
    protected static final VoxelShaper SHAPE = new AllShapes.Builder(Block.box(3, 3, 3, 13, 13, 13))
        .add(AllShapes.SIX_VOXEL_POLE.get(Axis.Y)).forAxis();

    public MechanicalGrindstoneBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        KineticBlockEntity grinder = getBlockEntity(level, pos);
        if (grinder == null) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos.below()) instanceof GrindstoneDrainBlockEntity drain))
            return InteractionResult.PASS;
        ItemStack extracted = drain.inventory.removeItemNoUpdate(0);
        if (extracted.isEmpty()) return InteractionResult.PASS;
        player.getInventory().placeItemBackInInventory(extracted);
        drain.setChanged();
        if (!player.isCreative()) {
            float speed = Math.abs(grinder.getSpeed());
            if (speed >= 32) player.hurt(CEIDamageSources.grind(level), speed / 32f);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        KineticBlockEntity grinder = getBlockEntity(level, pos);
        if (grinder == null) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (player.isSecondaryUseActive() || Math.abs(grinder.getSpeed()) < 30) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (level instanceof ServerLevel serverLevel) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            var polishing = serverLevel.recipeAccess().getRecipeFor(
                com.zurrtum.create.AllRecipeTypes.SANDPAPER_POLISHING, input, level
            );
            if (polishing.isPresent()) {
                if (!FakePlayerHandler.has(player) && player.getCooldowns().isOnCooldown(stack)) return InteractionResult.TRY_WITH_EMPTY_HAND;
                ItemStack polished = polishing.get().value().assemble(input, level.registryAccess());
                if (!FakePlayerHandler.has(player)) player.getCooldowns().addCooldown(stack, 10);
                SandPaperItem.spawnParticles(hitResult.getLocation(), stack, level);
                AllSoundEvents.SANDING_SHORT.play(level, player, pos, 1, 1);
                stack.shrink(1);
                CEIAdvancements.GRIND_TO_POLISH.awardTo(player);
                if (stack.isEmpty()) player.setItemInHand(hand, polished); else player.getInventory().placeItemBackInInventory(polished);
                return InteractionResult.SUCCESS;
            }
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var result = GrindstoneHelper.grindItem(level, stack, player.getItemInHand(otherHand));
        if (result.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        GrindstoneHelper.Result grinding = result.get();
        if (grinding.top().isEmpty()) player.setItemInHand(hand, grinding.output());
        else {
            player.setItemInHand(hand, grinding.top());
            player.getInventory().placeItemBackInInventory(grinding.output());
        }
        player.setItemInHand(otherHand, grinding.bottom());
        CEIAdvancements.GONE_WITH_THE_FOIL.awardTo(player);
        player.awardStat(CEIStats.GRINDSTONE_EXPERIENCE.get(), grinding.experience());
        if (player instanceof ServerPlayer serverPlayer) ExperienceHelper.award(grinding.experience(), serverPlayer);
        level.levelEvent(1042, pos, 0);
        return InteractionResult.SUCCESS;
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE.get(state.getValue(AXIS)); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return face.getAxis() == state.getValue(AXIS); }
    @Override public Class<KineticBlockEntity> getBlockEntityClass() { return KineticBlockEntity.class; }
    @Override public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() { return CEIBlockEntities.MECHANICAL_GRINDSTONE.get(); }
    @Override public Axis getRotationAxis(BlockState state) { return state.getValue(AXIS); }
}
