package plus.dragons.createenchantmentindustry.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

/** Bridges vanilla's stat counter update to CEI's stat advancement criterion. */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerStatMixin {
    @Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("TAIL"))
    private void cei$checkStatAdvancements(Stat<?> stat, int amount, CallbackInfo ci) {
        if (amount == 0) return;
        ServerPlayer player = (ServerPlayer) (Object) this;
        CEIAdvancements.STAT_TRIGGER.trigger(player, stat, player.getStats().getValue(stat));
    }
}
