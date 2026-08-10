/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringFanProcessingType;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.util.ItemStackKey;

/** Direct Fabric registration for the CDP Create filter attributes. */
public final class CDPItemAttributes {
    private static final Map<ItemStackKey, Boolean> STAINABLE_CACHE = new ConcurrentHashMap<>();

    public static final ItemAttributeType FREEZABLE = fanProcessing("freezable", CDPFanProcessingTypes.FREEZING);
    public static final ItemAttributeType SANDABLE = fanProcessing("sandable", CDPFanProcessingTypes.SANDING);
    public static final ItemAttributeType ENDABLE = fanProcessing("endable", CDPFanProcessingTypes.ENDING);
    public static final ItemAttributeType STAINABLE = colorFanProcessing(CDPFanProcessingTypes.COLORING.values());

    private CDPItemAttributes() {}

    private static ItemAttributeType fanProcessing(String name, Supplier<? extends FanProcessingType> processingType) {
        return Registry.register(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CDPCommon.asResource(name),
                new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(
                        type, processingType.get()::canProcess, CDPCommon.ID + "." + name)));
    }

    private static ItemAttributeType colorFanProcessing(Collection<Supplier<ColoringFanProcessingType>> processingTypes) {
        return Registry.register(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CDPCommon.asResource("stainable"),
                new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type,
                        (itemStack, level) -> canProcessByColoring(itemStack, level, processingTypes),
                        CDPCommon.ID + ".stainable")));
    }

    private static boolean canProcessByColoring(ItemStack stack, Level level, Collection<Supplier<ColoringFanProcessingType>> processingTypes) {
        if (!CDPConfig.recipes().enableBulkColoring.get())
            return false;
        return STAINABLE_CACHE.computeIfAbsent(ItemStackKey.of(stack), key -> processingTypes.stream()
                .anyMatch(s -> s.get().canProcess(stack, level)));
    }

    public static void recreateCache() {
        STAINABLE_CACHE.clear();
    }

    public static void register() {
        // Static initialisation performs Fabric registry registration.
    }
}
