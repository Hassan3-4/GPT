/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import plus.dragons.createdragonsplus.client.texture.CDPGuiTextures;
import plus.dragons.createdragonsplus.common.CDPCommon;

/** CEI-required item registrations for the 26.2 compatibility build. */
public final class CDPItems {
    public static final SmithingTemplateItem BLAZE_UPGRADE_SMITHING_TEMPLATE = register(
            "blaze_upgrade_smithing_template",
            properties -> new SmithingTemplateItem(
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
}
