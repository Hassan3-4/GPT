/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIArmInterationPoints;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEICreativeModeTabs;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItemAttributes;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.common.registry.CEIMountedStorageTypes;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.common.registry.CEIResourceConditions;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Fabric common entry point. Registries are direct vanilla/Fabric registrations. */
public final class CEICommon implements ModInitializer {
    public static final String ID = "create_enchantment_industry";
    @Override
    public void onInitialize() {
        CEIConfig.register();
        CEIResourceConditions.register();
        CEIItems.register();
        CEIFluids.register();
        CEIBlocks.register();
        CEIBlockEntities.register();
        CEIMountedStorageTypes.register();
        CEIArmInterationPoints.register();
        CEIRecipes.register();
        CEIEnchantments.register();
        CEIDataMaps.register();
        CEIItemAttributes.register();
        CEIStats.register();
        CEICreativeModeTabs.register();
        CEIAdvancements.register();
        CEIAdvancements.BuiltinTriggersQuickDeploy.register();
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static String asLocalization(String key) {
        return ID + "." + key;
    }
}
