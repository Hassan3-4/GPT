package plus.dragons.createenchantmentindustry.mixin.client;

import java.util.List;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.tank.FluidTankBehaviour;

@Mixin(ExperienceLanternBlockEntity.class)
public abstract class ExperienceLanternGoggleMixin implements IHaveGoggleInformation {
    @Shadow protected FluidTankBehaviour tank;

    @Overwrite
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
    }
}
