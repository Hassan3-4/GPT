/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.common.fluids.dye.RegisterDyeVariantsEvent;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPCauldrons;
import plus.dragons.createdragonsplus.common.registry.CDPCreativeModeTabs;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.common.registry.CDPItemAttributes;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.integration.CDPIntegrationContributions;
import plus.dragons.createdragonsplus.integration.ModIntegration;
import plus.dragons.createdragonsplus.integration.arts_and_crafts.ArtsAndCraftsDyeVariants;
import plus.dragons.createdragonsplus.integration.dye_depot.DyeDepotDyeVariants;
import plus.dragons.createdragonsplus.integration.dyenamics.DyenamicsDyeVariants;

/** Fabric common entry point for Create: Dragons Plus. */
public final class CDPCommon implements ModInitializer {
    public static final String ID = "create_dragons_plus";
    public static final String NAME = "Create: Dragons Plus";
    public static final String PERSISTENT_DATA_KEY = "CreateDragonsPlusData";

    @Override
    public void onInitialize() {
        // Feature flags must be materialised before any registry reads them.
        CDPConfig.register();
        bootstrapDyeVariants();

        // These three registrations are already direct Fabric registrations.  The
        // remaining CDP registries are initialised by their Fabric counterparts as
        // they are migrated, rather than through a NeoForge event bus.
        CDPBlocks.register();
        CDPBlockEntities.register();
        CDPItems.register();
        CDPFluids.register();
        CDPCauldrons.register();
        CDPRecipes.register();
        CDPFanProcessingTypes.register();
        CDPItemAttributes.register();
        CDPCreativeModeTabs.register();
    }

    private static void bootstrapDyeVariants() {
        if (DyeVariantRegistry.isFrozen())
            return;

        var builder = new DyeVariantRegistry.Builder();
        DyeColors.registerVanilla(builder);
        if (ModIntegration.ARTS_AND_CRAFTS.enabled())
            ArtsAndCraftsDyeVariants.register(new RegisterDyeVariantsEvent(builder));
        if (ModIntegration.DYE_DEPOT.enabled())
            DyeDepotDyeVariants.register(new RegisterDyeVariantsEvent(builder));
        if (ModIntegration.DYENAMICS.enabled())
            DyenamicsDyeVariants.register(new RegisterDyeVariantsEvent(builder));
        CDPIntegrationContributions.gatherDyeVariants(new RegisterDyeVariantsEvent(builder));
        DyeVariantRegistry.freeze(builder.build());
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
