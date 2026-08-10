/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPItems;

/**
 * Fabric common entry point for the Minecraft 26.2 CEI compatibility build.
 *
 * <p>This intentionally registers only the Create: Dragons Plus pieces used by
 * Create: Enchantment Industry: the Fluid Hatch and Blaze Upgrade smithing
 * template. Unrelated CDP dye-fluid, cauldron, fan-processing and integration
 * systems remain in source history but are not part of this compatibility jar.</p>
 */
public final class CDPCommon implements ModInitializer {
    public static final String ID = "create_dragons_plus";
    public static final String NAME = "Create: Dragons Plus (CEI Compatibility)";
    public static final String PERSISTENT_DATA_KEY = "CreateDragonsPlusData";

    @Override
    public void onInitialize() {
        CDPBlocks.register();
        CDPBlockEntities.register();
        CDPItems.register();
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
