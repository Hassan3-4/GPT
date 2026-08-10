package plus.dragons.createenchantmentindustry.mixin.client;

import java.util.List;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;

@Mixin(PrinterBlockEntity.class)
public abstract class PrinterGoggleMixin implements IHaveGoggleInformation {
    @Shadow protected SmartFluidTankBehaviour tank;
    @Shadow private PrinterBehaviour printer;

    @Overwrite
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getPrimaryHandler());
        return printer.getPrintingBehaviour().addToGoggleTooltip(tooltip, isPlayerSneaking) || added;
    }
}
