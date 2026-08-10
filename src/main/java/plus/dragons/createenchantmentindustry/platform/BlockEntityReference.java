package plus.dragons.createenchantmentindustry.platform;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Lazy direct-registry lookup for a CEI block entity type. */
public final class BlockEntityReference<T extends net.minecraft.world.level.block.entity.BlockEntity> implements Supplier<BlockEntityType<T>> {
    private final Registry<BlockEntityType<?>> registry;
    private final Identifier id;

    public BlockEntityReference(Registry<BlockEntityType<?>> registry, Identifier id) {
        this.registry = Objects.requireNonNull(registry);
        this.id = Objects.requireNonNull(id);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockEntityType<T> get() {
        return (BlockEntityType<T>) (BlockEntityType) registry.get(id)
            .map(net.minecraft.core.Holder.Reference::value)
            .orElseThrow(() -> new IllegalStateException("Missing block entity registry entry " + id));
    }

    public Identifier getId() { return id; }
}
