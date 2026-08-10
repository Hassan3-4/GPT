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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import java.util.List;
import java.util.function.Consumer;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancements.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

@FieldsNullabilityUnknownByDefault
public class BlazeForgerBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    public static final int FORGING_TIME = 200;
    protected boolean special;
    protected boolean cursed;
    protected int processingTime = -1;
    protected final BlazeForgerInventory inventory;
    protected AdvancementBehaviour advancement;

    public BlazeForgerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new BlazeForgerInventory(this);
    }

    public @Nullable FluidInventory getFluidHandler(@Nullable Direction side) {
        if ((side == Direction.DOWN || side == null) && !isRemoved())
            return tanks.getCapability();
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        this.advancement = new AdvancementBehaviour(this);
        behaviours.add(this.advancement);
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(
                FluidUnits.fromMillibuckets(CEIConfig.fluids().blazeForgerFluidCapacity.get()),
                fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.isOf(CEIFluids.EXPERIENCE));
    }

    @Override
    protected ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(
                FluidUnits.fromMillibuckets(CEIConfig.fluids().blazeForgerFluidCapacity.get()),
                fluidUpdateCallback)
                .forbidInsertion();
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected @Nullable PartialModel getHatModel(HeatLevel heatLevel) {
        return heatLevel.isAtLeast(HeatLevel.FADING)
                ? CEIPartialModels.BLAZE_FORGER_HAT
                : CEIPartialModels.BLAZE_FORGER_HAT_SMALL;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("ProcessingTime", processingTime);
        inventory.write(view.child("Inventory"));
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        processingTime = view.getIntOr("ProcessingTime", -1);
        inventory.read(view.childOrEmpty("Inventory"));
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            Containers.dropContents(level, worldPosition, inventory);
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean update = false;
        boolean special = getHeatLevelFromBlock() == HeatLevel.SEETHING;
        if (this.special != special) {
            this.special = special;
            update = true;
        }
        var strikePos = getStrikePos();
        boolean cursed = special && !worldPosition.equals(strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
            update = true;
        }
        if (level.isClientSide() && isVirtual()) {
            if (update) {
                inventory.updateResult();
                notifyUpdate();
            }
            var cost = inventory.getExperienceCost();
            if (cost > 0 && consumeExperience(cost, special, true)) {
                if (processingTime < 0) {
                    processingTime = FORGING_TIME / 4;
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    return;
                }
                consumeExperience(cost, special, false);
                processingTime = -1;
                inventory.applyResult();
            } else if (processingTime != -1) processingTime = -1;
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            inventory.updateResult();
            notifyUpdate();
        }
        var cost = inventory.getExperienceCost();
        if (cost > 0 && consumeExperience(cost, special, true)) {
            if (processingTime < 0) {
                processingTime = FORGING_TIME;
                notifyUpdate();
                return;
            }
            if (processingTime > 0) {
                processingTime--;
                notifyUpdate();
                return;
            }
            if (special && !cursed && strikeLightning(serverLevel, strikePos)) {
                advancement.trigger(CEIAdvancements.OSHA_VIOLATION.builtinTrigger());
                serverLevel.destroyBlock(worldPosition, false);
                serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.defaultBlockState());
                this.setRemoved();
                return;
            }
            consumeExperience(cost, special, false);
            processingTime = -1;
            inventory.applyResult();
            notifyUpdate();
            level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        } else if (processingTime != -1) {
            processingTime = -1;
            notifyUpdate();
        }
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        var original = stack;
        if (inventory.hasRemainingOutput()) return stack;
        if (!stack.isEmpty())
            stack = inventory.insertItem(0, stack, simulate);
        if (!stack.isEmpty())
            stack = inventory.insertItem(1, stack, simulate);
        if (!simulate && (original.getCount() != stack.getCount() || !ItemStack.isSameItemSameComponents(original, stack))) {
            inventory.updateResult();
            notifyUpdate();
        }
        return stack;
    }

    public ItemStack extractItem(boolean simulate) {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack extracted = inventory.extractItem(i, 1, simulate);
            if (!extracted.isEmpty()) {
                if (!simulate && i < 2) {
                    inventory.updateResult();
                    notifyUpdate();
                }
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        var style = special
                ? (cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        int cost = inventory.getExperienceCost();
        if (cost > 0) {
            added = true;
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.forging.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            for (int i = 0; i < 2; i++) {
                var result = inventory.getResult(i);
                if (result.isEmpty())
                    continue;
                CEILang.translate("gui.goggles.forging.result").forGoggles(tooltip);
                CEILang.item(result).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
                if (!enchantments.isEmpty())
                    enchantments.addToTooltip(
                            TooltipContext.of(level),
                            component -> CEILang.builder().add(component).forGoggles(tooltip, 2),
                            TooltipFlag.NORMAL,
                            result);
            }
        } else {
            if (inventory.forgingCompleted())
                CEILang.translate("gui.goggles.forging.completed").style(ChatFormatting.GREEN).forGoggles(tooltip);
            else if (!inventory.notEnoughItemToForge()) {
                if (inventory.incompatibleEnchantingTemplateType())
                    CEILang.translate("gui.goggles.forging.invalid_template_type." + (special ? "normal" : "special")).style(ChatFormatting.RED).forGoggles(tooltip);
                else CEILang.translate("gui.goggles.forging.invalid_items").style(ChatFormatting.RED).forGoggles(tooltip);
            }
        }
        return added;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }
}
