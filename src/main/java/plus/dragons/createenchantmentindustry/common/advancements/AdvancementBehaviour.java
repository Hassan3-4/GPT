package plus.dragons.createenchantmentindustry.common.advancements;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/**
 * Persists the player who placed a CEI machine.  Machine progress performed
 * while that player is offline is retained and applied with the next action.
 */
public final class AdvancementBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<AdvancementBehaviour> TYPE = new BehaviourType<>(
        CEICommon.asResource("advancement").toString());
    private static final Codec<Map<Identifier, Integer>> STATS_CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT);

    private @Nullable UUID owner;
    private Map<Identifier, Integer> pendingStats = new HashMap<>();

    public AdvancementBehaviour(SmartBlockEntity blockEntity) { super(blockEntity); }

    public static void setPlacedBy(Level level, BlockPos pos, @Nullable LivingEntity placer) {
        if (level.isClientSide() || !(placer instanceof ServerPlayer player) || FakePlayerHandler.has(player)) return;
        AdvancementBehaviour behaviour = BlockEntityBehaviour.get(level, pos, TYPE);
        if (behaviour != null) behaviour.setOwner(player.getUUID());
    }

    public void setOwner(@Nullable UUID nextOwner) {
        if (!java.util.Objects.equals(owner, nextOwner)) pendingStats.clear();
        owner = nextOwner;
        blockEntity.setChanged();
    }

    private @Nullable ServerPlayer getOwner() {
        if (owner == null || getLevel().isClientSide()) return null;
        return getLevel().getPlayerByUUID(owner) instanceof ServerPlayer player && !FakePlayerHandler.has(player) ? player : null;
    }

    public void trigger(BuiltinTrigger trigger) {
        ServerPlayer player = getOwner();
        if (player != null) trigger.trigger(player);
    }

    public void awardStat(Identifier stat, int amount) {
        if (amount == 0) return;
        ServerPlayer player = getOwner();
        if (player == null) {
            if (owner != null) pendingStats.merge(stat, amount, Integer::sum);
            return;
        }
        player.awardStat(Stats.CUSTOM.get(stat), amount + pendingStats.getOrDefault(stat, 0));
        pendingStats.remove(stat);
        blockEntity.setChanged();
    }

    @Override public BehaviourType<?> getType() { return TYPE; }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (clientPacket) return;
        if (owner != null) view.store("Owner", UUIDUtil.CODEC, owner);
        if (!pendingStats.isEmpty()) view.store("PendingStats", STATS_CODEC, pendingStats);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        if (clientPacket) return;
        owner = view.read("Owner", UUIDUtil.CODEC).orElse(null);
        // Codec.unboundedMap may return Guava's ImmutableMap. The behaviour
        // later merges and removes entries as the owning player comes online,
        // so always detach decoded data into a mutable map. Without this copy,
        // the first completed machine operation after a chunk reload crashes
        // the server in ImmutableMap.remove().
        pendingStats = new HashMap<>(view.read("PendingStats", STATS_CODEC).orElseGet(Map::of));
    }
}
