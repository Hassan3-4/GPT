package plus.dragons.createenchantmentindustry.platform;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Fabric registry reference retaining the useful ItemEntry operations CEI needs. */
public final class ItemReference<T extends Item> implements Supplier<T>, ItemLike {
    private final Registry<Item> registry;
    private final Identifier id;

    public ItemReference(Registry<Item> registry, Identifier id) {
        this.registry = Objects.requireNonNull(registry);
        this.id = Objects.requireNonNull(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        Item item = registry.get(id).map(net.minecraft.core.Holder.Reference::value)
            .orElseThrow(() -> new IllegalStateException("Missing item registry entry " + id));
        return (T) item;
    }

    @Override public Item asItem() { return get(); }
    public ItemStack asStack() { return new ItemStack(get()); }
    public Identifier getId() { return id; }
}
