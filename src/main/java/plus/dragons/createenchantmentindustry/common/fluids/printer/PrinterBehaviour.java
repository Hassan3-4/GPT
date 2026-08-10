/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.RecipePrintingBehaviour;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;

/** Server-side printer template filter.  Its client value-box counterpart is
 * registered from CEIClient so dedicated servers never load rendering code. */
public class PrinterBehaviour extends ServerFilteringBehaviour {
    /**
     * Create Fly's client-side {@code FilteringBehaviour} locates its server
     * counterpart through {@link ServerFilteringBehaviour#TYPE}.  Keep the
     * strongly typed CEI alias, but use the exact same behaviour-type identity
     * so the client value box can bind to this custom implementation.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BehaviourType<PrinterBehaviour> TYPE =
        (BehaviourType) ServerFilteringBehaviour.TYPE;
    public static final String TEMPLATE = "PrintingTemplate";

    private final SmartFluidTankBehaviour tank;
    private PrintingBehaviour printing = new RecipePrintingBehaviour(ItemStack.EMPTY);

    public PrinterBehaviour(SmartBlockEntity blockEntity, SmartFluidTankBehaviour tank) {
        super(blockEntity);
        this.tank = tank;
    }

    public PrintingBehaviour getPrintingBehaviour() { return printing; }

    public boolean setFilter(ItemStack stack, @Nullable Player player) {
        var result = PrintingBehaviour.create(getLevel(), tank, stack).resultOrPartial(message -> {
            if (player != null) player.displayClientMessage(Component.translatable(message), true);
        });
        if (result.isEmpty() || !super.setFilter(stack)) return false;
        printing = result.get();
        return true;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        return setFilter(stack, null);
    }

    @Override public BehaviourType<?> getType() { return TYPE; }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store(TEMPLATE, FilterItemStack.CODEC, filter);
    }

    @Override
    public void writeSafe(ValueOutput view) {
        if (printing.isSafeNBT()) write(view, false);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        FilterItemStack saved = view.read(TEMPLATE, FilterItemStack.CODEC).orElseGet(() -> FilterItemStack.of(getFilter()));
        PrintingBehaviour.create(getLevel(), tank, saved.item()).result().ifPresentOrElse(found -> {
            filter = saved;
            printing = found;
        }, () -> {
            filter = FilterItemStack.empty();
            printing = RecipePrintingBehaviour.EMPTY;
        });
    }

    @Override public String getClipboardKey() { return "Printer"; }

    @Override
    public boolean writeToClipboard(ValueOutput view, Direction side) {
        super.writeToClipboard(view, side);
        view.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, getFilter());
        return true;
    }

    @Override
    public boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate) {
        if (!super.readFromClipboard(view, player, side, simulate)) return false;
        var template = view.read(TEMPLATE, ItemStack.OPTIONAL_CODEC);
        if (template.isEmpty() || simulate || getLevel().isClientSide()) return template.isPresent();
        return setFilter(template.get(), player);
    }
}
