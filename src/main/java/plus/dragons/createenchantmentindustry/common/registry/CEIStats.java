/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Custom statistics registered directly in the vanilla Fabric-visible registry. */
public final class CEIStats {
    public static final Supplier<Identifier> GRINDSTONE_EXPERIENCE = create("mechanical_grindstone_experience");
    public static final Supplier<Identifier> SUPER_ENCHANT = create("super_enchant");
    public static final Supplier<Identifier> PRINT = create("print");
    public static final Supplier<Identifier> FORGE = create("forge");
    public static final Supplier<Identifier> ENCHANT = create("enchant");
    public static final Supplier<Identifier> CLASSIC_ENCHANT = create("classic_enchant");

    private CEIStats() {}

    private static Supplier<Identifier> create(String path) {
        Identifier id = CEICommon.asResource(path);
        Identifier registered = Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        return () -> registered;
    }

    public static void register() {
        // Static initialisation performs registrations before the registry freezes.
    }
}
