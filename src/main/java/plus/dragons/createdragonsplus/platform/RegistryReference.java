package plus.dragons.createdragonsplus.platform;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/** Late registry lookup for optional-mod content on Fabric. */
public record RegistryReference<T>(Registry<T> registry, Identifier id) implements Supplier<T> {
    @Override
    public T get() {
        T value = registry.getValue(id);
        if (value == null) {
            throw new IllegalStateException("Missing registry entry " + id);
        }
        return value;
    }

    public Optional<T> asOptional() {
        return registry.getOptional(id);
    }
}
