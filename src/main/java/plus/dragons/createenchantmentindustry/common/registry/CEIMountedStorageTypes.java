package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedFluidStorageType;

/** Mounted storage registration for the experience lantern. */
public final class CEIMountedStorageTypes {
    public static final ExperienceLanternMountedFluidStorageType EXPERIENCE_LANTERN = Registry.register(
        CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE,
        ResourceKey.create(CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, CEICommon.asResource("experience_lantern")),
        new ExperienceLanternMountedFluidStorageType()
    );

    private CEIMountedStorageTypes() {}

    public static void register() {
        AllMountedStorageTypes.register(EXPERIENCE_LANTERN, CEIBlocks.EXPERIENCE_LANTERN.get());
    }
}
