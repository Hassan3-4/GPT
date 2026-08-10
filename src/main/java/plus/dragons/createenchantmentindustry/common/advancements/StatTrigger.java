package plus.dragons.createenchantmentindustry.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

/**
 * Criterion for CEI's machine statistics.
 *
 * <p>Fabric does not expose NeoForge's stat-award event, so the matching
 * callback is made by {@code ServerPlayerStatMixin} after vanilla has updated
 * the stat counter.  Keeping this as a real criterion (rather than awarding
 * achievements directly) preserves datapack reload and custom advancement
 * behaviour.</p>
 */
public final class StatTrigger implements CriterionTrigger<StatTrigger.Conditions> {
    private final Map<PlayerAdvancements, Map<Identifier, Set<Listener<Conditions>>>> listeners = new IdentityHashMap<>();

    public void trigger(ServerPlayer player, Stat<?> stat, int total) {
        // ServerPlayer#awardStat is invoked for every vanilla stat type as
        // well (used/mined/crafted/etc.). CEI advancements only subscribe to
        // custom machine stats, so unrelated item placement must be ignored.
        if (stat.getType() != Stats.CUSTOM) return;
        trigger(player, customStatId(stat), total);
    }

    public void trigger(ServerPlayer player, Identifier stat, int total) {
        Map<Identifier, Set<Listener<Conditions>>> byStat = listeners.get(player.getAdvancements());
        if (byStat == null) return;
        Set<Listener<Conditions>> entries = byStat.get(stat);
        if (entries == null) return;
        for (Listener<Conditions> listener : Set.copyOf(entries)) {
            if (listener.trigger().bounds().matches(total)) listener.run(player.getAdvancements());
        }
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Conditions> listener) {
        listeners.computeIfAbsent(advancements, ignored -> new HashMap<>())
            .computeIfAbsent(listener.trigger().stat(), ignored -> new HashSet<>())
            .add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Conditions> listener) {
        Map<Identifier, Set<Listener<Conditions>>> byStat = listeners.get(advancements);
        if (byStat == null) return;
        Set<Listener<Conditions>> entries = byStat.get(listener.trigger().stat());
        if (entries == null) return;
        entries.remove(listener);
        if (entries.isEmpty()) byStat.remove(listener.trigger().stat());
        if (byStat.isEmpty()) listeners.remove(advancements);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.remove(advancements);
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Identifier stat, MinMaxBounds.Ints bounds) implements CriterionTriggerInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("value").forGetter(Conditions::stat),
            MinMaxBounds.Ints.CODEC.fieldOf("bounds").forGetter(Conditions::bounds)
        ).apply(instance, Conditions::new));

        public static Criterion<Conditions> of(Stat<?> stat, MinMaxBounds.Ints bounds) {
            return CEIAdvancements.STAT_TRIGGER.createCriterion(new Conditions(customStatId(stat), bounds));
        }

        @Override
        public void validate(CriterionValidator validator) {}
    }

    private static Identifier customStatId(Stat<?> stat) {
        if (stat.getType() != Stats.CUSTOM) {
            throw new IllegalArgumentException("CEI stat advancement criteria require a custom stat");
        }
        return (Identifier) stat.getValue();
    }
}
