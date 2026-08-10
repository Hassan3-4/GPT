/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingFanProcessingType;

/** Direct Fabric registration for CDP's Create fan processing types. */
public final class CDPFanProcessingTypes {
    public static final Map<Identifier, Supplier<ColoringFanProcessingType>> COLORING = registerColoring();
    private static final FreezingFanProcessingType FREEZING_VALUE = register("freezing", new FreezingFanProcessingType());
    private static final SandingFanProcessingType SANDING_VALUE = register("sanding", new SandingFanProcessingType());
    private static final EndingFanProcessingType ENDING_VALUE = register("ending", new EndingFanProcessingType());
    public static final Supplier<FreezingFanProcessingType> FREEZING = () -> FREEZING_VALUE;
    public static final Supplier<SandingFanProcessingType> SANDING = () -> SANDING_VALUE;
    public static final Supplier<EndingFanProcessingType> ENDING = () -> ENDING_VALUE;

    private CDPFanProcessingTypes() {}

    private static Map<Identifier, Supplier<ColoringFanProcessingType>> registerColoring() {
        var entries = ImmutableMap.<Identifier, Supplier<ColoringFanProcessingType>>builder();
        for (var variant : DyeVariantRegistry.all()) {
            var type = register(variant.fanProcessingName(), new ColoringFanProcessingType(variant));
            entries.put(variant.id(), () -> type);
        }
        return entries.build();
    }

    private static <T extends FanProcessingType> T register(String path, T type) {
        return Registry.register(CreateRegistries.FAN_PROCESSING_TYPE, CDPCommon.asResource(path), type);
    }

    public static void register() {
        // Static initialisation performs Fabric registry registration.
    }
}
