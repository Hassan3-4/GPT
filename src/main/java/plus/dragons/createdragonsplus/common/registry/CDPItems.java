/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.box.PackageStyles.PackageStyle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import plus.dragons.createdragonsplus.client.texture.CDPGuiTextures;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;

/** Fabric registry for CDP items and the runtime tag keys they consume. */
public final class CDPItems {
    public static final CommonTags COMMON_TAGS = new CommonTags();
    public static final ModTags MOD_TAGS = new ModTags();

    // Keep the historical misspelled registry ids: changing them would break saved item stacks.
    public static final PackageItem RARE_BLAZE_PACKAGE = register("rare_blaze_pacakge", properties ->
            new PackageItem(properties.stacksTo(1).fireResistant(),
                    new PackageStyle("rare_blaze", 12, 10, 21, true)));
    public static final PackageItem RARE_MARBLE_GATE_PACKAGE = register("rare_marble_gate_pacakge", properties ->
            new PackageItem(properties.stacksTo(1),
                    new PackageStyle("rare_marble_gate", 12, 10, 21, true)));
    public static final SmithingTemplateItem BLAZE_UPGRADE_SMITHING_TEMPLATE = register("blaze_upgrade_smithing_template", properties ->
            new SmithingTemplateItem(
                    Tooltips.BLAZE_UPGRADE_APPLIES_TO,
                    Tooltips.BLAZE_UPGRADE_INGREDIENTS,
                    Tooltips.BLAZE_UPGRADE_BASE_SLOT,
                    Tooltips.BLAZE_UPGRADE_ADDITIONS_SLOT,
                    CDPGuiTextures.BLAZE_UPGRADE_BASE_SLOT_ICONS,
                    CDPGuiTextures.BLAZE_UPGRADE_ADDITIONS_SLOT_ICONS,
                    properties));

    private CDPItems() {}

    private static <T extends Item> T register(String path, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, CDPCommon.asResource(path));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    public static void register() {
        // Static initialization performs Fabric registry registration.
    }

    public static final class Tooltips {
        private static final String PREFIX = "item.create_dragons_plus.smithing_template.blaze_upgrade.";
        public static final Component BLAZE_UPGRADE_APPLIES_TO = Component.translatable(PREFIX + "applies_to")
                .withStyle(ChatFormatting.BLUE);
        public static final Component BLAZE_UPGRADE_INGREDIENTS = Component.translatable(PREFIX + "ingredients")
                .withStyle(ChatFormatting.BLUE);
        public static final Component BLAZE_UPGRADE = Component.translatable("upgrade.create_dragons_plus.blaze_upgrade")
                .withStyle(ChatFormatting.GRAY);
        public static final Component BLAZE_UPGRADE_BASE_SLOT = Component.translatable(PREFIX + "base_slot_description");
        public static final Component BLAZE_UPGRADE_ADDITIONS_SLOT = Component.translatable(PREFIX + "additions_slot_description");
    }

    public static final class CommonTags {
        public final TagKey<Item> dyeBuckets = tag("c", "buckets/dye");
        public final Map<Identifier, TagKey<Item>> dyeBucketsByVariant = new LinkedHashMap<>();
        public final TagKey<Item> dragonBreathBuckets = tag("c", "buckets/dragon_breath");

        private CommonTags() {
            for (var variant : DyeVariantRegistry.all()) {
                dyeBucketsByVariant.put(variant.id(), tag("c", "buckets/dye/" + variant.serializedName()));
            }
        }
    }

    public static final class ModTags {
        public final TagKey<Item> notApplicableColoring = tag(CDPCommon.ID, "not_applicable_for_coloring");
        public final Map<Identifier, TagKey<Item>> dyeItemsByVariant = new LinkedHashMap<>();

        private ModTags() {
            for (var variant : DyeVariantRegistry.all()) {
                dyeItemsByVariant.put(variant.id(), variant.dyeItemTag());
            }
        }
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
