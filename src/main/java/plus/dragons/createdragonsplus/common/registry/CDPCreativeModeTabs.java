/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.config.CDPConfig;

/** Fabric creative-tab registration preserving the CDP item visibility rules. */
public final class CDPCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> BASE = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, CDPCommon.asResource("base"));

    private CDPCreativeModeTabs() {}

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BASE,
                CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("itemGroup.create_dragons_plus.base"))
                        .icon(CDPItems.RARE_MARBLE_GATE_PACKAGE::getDefaultInstance)
                        .displayItems(CDPCreativeModeTabs::buildBaseContents)
                        .build());
    }

    private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (CDPConfig.features().fluidHatch.get())
            output.accept(CDPBlocks.FLUID_HATCH);
        if (CDPConfig.features().blazeUpgradeSmithingTemplate.get())
            output.accept(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE);
        if (CDPConfig.features().dyeFluids.get()) {
            for (var variant : DyeVariantRegistry.all()) {
                if (!variant.isAvailable())
                    continue;
                var fluid = CDPFluids.DYES_BY_VARIANT.get(variant.id());
                if (fluid != null)
                    output.accept(fluid.bucket());
            }
        }
        if (CDPConfig.features().dragonBreathFluid.get())
            output.accept(CDPFluids.DRAGON_BREATH.bucket());
        output.accept(CDPItems.RARE_BLAZE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
        output.accept(CDPItems.RARE_MARBLE_GATE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
    }
}
