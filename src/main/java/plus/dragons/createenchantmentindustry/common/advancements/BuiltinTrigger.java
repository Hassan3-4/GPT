package plus.dragons.createenchantmentindustry.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

/** CEI's data-driven criterion trigger for machine actions. */
public final class BuiltinTrigger implements CriterionTrigger<BuiltinTrigger.Conditions> {
    private final Map<PlayerAdvancements, Set<Listener<Conditions>>> listeners = new IdentityHashMap<>();

    public void trigger(ServerPlayer player) {
        Set<Listener<Conditions>> entries = listeners.get(player.getAdvancements());
        if (entries != null) entries.forEach(listener -> listener.run(player.getAdvancements()));
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Conditions> listener) {
        listeners.computeIfAbsent(advancements, ignored -> new java.util.HashSet<>()).add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Conditions> listener) {
        Set<Listener<Conditions>> entries = listeners.get(advancements);
        if (entries == null) return;
        entries.remove(listener);
        if (entries.isEmpty()) listeners.remove(advancements);
    }

    @Override public void removePlayerListeners(PlayerAdvancements advancements) { listeners.remove(advancements); }
    @Override public Codec<Conditions> codec() { return Conditions.CODEC; }

    public static final class Conditions implements CriterionTriggerInstance {
        private static final Codec<Conditions> CODEC = MapCodec.unitCodec(new Conditions());
        @Override public void validate(CriterionValidator validator) {}
    }
}
