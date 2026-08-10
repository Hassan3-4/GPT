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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import java.util.List;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.EnchantingBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.TemplateEnchantingBehaviour;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchanterBehaviour extends ServerScrollValueBehaviour {
    /** Create Fly resolves client value-setting packets through this shared server type. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BehaviourType<EnchanterBehaviour> TYPE =
        (BehaviourType) ServerScrollValueBehaviour.TYPE;
    public static final String LEVEL = "EnchantingLevel";
    public static final String TEMPLATE = "EnchantingTemplate";
    private final BlazeEnchanterBlockEntity enchanter;
    private ItemStack template = ItemStack.EMPTY;
    private EnchantingBehaviour enchanting = new EnchantingBehaviour();
    public EnchanterBehaviour(BlazeEnchanterBlockEntity enchanter) {
        super(enchanter);
        this.enchanter = enchanter;
    }

    public boolean canProcess(ItemStack stack) {
        return enchanting.canProcess(getLevel(), stack, enchanter.special);
    }

    public void update(ItemStack stack) {
        enchanting.update(getLevel(), stack, value, enchanter.special, enchanter.cursed);
    }

    public ItemStack getResult(ItemStack stack) {
        return enchanting.getResult(getLevel(), stack, enchanter.getRandom(), enchanter.special);
    }

    public int getExperienceCost() {
        return enchanting.getExperienceCost();
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean setTemplate(ItemStack stack) {
        if (!loadTemplate(stack))
            return false;
        update(enchanter.heldItem);
        var level = getLevel();
        if (!level.isClientSide()) {
            blockEntity.setChanged();
            blockEntity.sendData();
        }
        return true;
    }

    private boolean loadTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
            enchanting = new EnchantingBehaviour();
        } else if (stack.isEnchantable()) {
            template = stack;
            enchanting = new TemplateEnchantingBehaviour(template);
        } else return false;
        return true;
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, enchanter.getMaxEnchantLevel());
        if (value == this.value)
            return;
        this.value = value;
        update(enchanter.heldItem);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        if (stack.is(AllItems.WRENCH))
            return;
        if (stack.is(AllItems.MECHANICAL_ARM))
            return;
        var level = getLevel();
        var pos = getPos();
        if (stack.isEmpty()) {
            setTemplate(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .25f, .1f);
            return;
        }
        if (!setTemplate(stack.copyWithCount(1))) {
            player.displayClientMessage(CEILang.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt(LEVEL, value);
        view.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, template);
    }

    @Override
    public void writeSafe(ValueOutput view) {
        view.putInt(LEVEL, value);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        value = Math.clamp(view.getIntOr(LEVEL, 0), 0, enchanter.getMaxEnchantLevel());
        loadTemplate(view.read(TEMPLATE, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void initialize() {
        loadTemplate(template);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = false;
        if (!template.isEmpty()) {
            CEILang.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            added = true;
        }
        var style = enchanter.special
                ? (enchanter.cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        if (value > 0) {
            CEILang.translate("gui.goggles.enchanting.level", CEILang.number(value).style(style))
                    .forGoggles(tooltip);
            added = true;
        } else {
            CEILang.translate("gui.goggles.enchanting.level.not_set").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        int cost = getExperienceCost();
        if (cost > 0) {
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.enchanting.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            added = true;
        }
        if (!enchanter.heldItem.isEmpty() && enchanter.processingTime == -1) {
            if (!EnchantmentHelper.getEnchantmentsForCrafting(enchanter.heldItem).isEmpty()) {
                CEILang.translate("gui.goggles.enchanting.completed").style(ChatFormatting.GREEN).forGoggles(tooltip);
            } else if (cost > 0) {
                int experience = enchanter.special ? enchanter.getSpecialExperience() : enchanter.getNormalExperience();
                if (experience < cost) {
                    CEILang.translate("gui.goggles.enchanting.insufficient_experience").style(ChatFormatting.RED).forGoggles(tooltip);
                }
            } else {
                CEILang.translate("gui.goggles.enchanting.invalid_item").style(ChatFormatting.RED).forGoggles(tooltip);
            }
        }
        return added;
    }
}
