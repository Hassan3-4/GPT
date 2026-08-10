package plus.dragons.createenchantmentindustry.platform;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Direct Fabric block registry reference.  It deliberately retains the small
 * ItemLike/Supplier surface used by CEI recipes, Ponder scenes and arm points,
 * without retaining Registrate as a runtime dependency.
 */
public final class BlockReference<T extends Block> implements Supplier<T>, ItemLike {
    private final Registry<Block> registry;
    private final Identifier id;

    public BlockReference(Registry<Block> registry, Identifier id) {
        this.registry = Objects.requireNonNull(registry);
        this.id = Objects.requireNonNull(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) registry.get(id)
            .map(net.minecraft.core.Holder.Reference::value)
            .orElseThrow(() -> new IllegalStateException("Missing block registry entry " + id));
    }

    @Override public Item asItem() { return get().asItem(); }
    public ItemStack asStack() { return new ItemStack(get()); }
    public BlockState getDefaultState() { return get().defaultBlockState(); }
    public boolean has(BlockState state) { return state.is(get()); }
    public Identifier getId() { return id; }
}
