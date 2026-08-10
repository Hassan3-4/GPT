/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/**
 * CEI's enchantment tag keys.
 *
 * <p>Tag contents are data-pack data and therefore need no loader-specific
 * registration on Fabric.  Keeping the exact ids preserves existing packs and
 * lets Create Fly's normal tag reload path own their contents.</p>
 */
public final class CEIEnchantments {
    public static final ModTags MOD_TAGS = new ModTags();

    private CEIEnchantments() {}

    public static void register() {
        // Loading this class materialises the tag keys; JSON tags are reloaded by Minecraft.
    }

    public static final class ModTags {
        public final TagKey<Enchantment> enchanting = tag("blaze_enchanter/enchanting");
        public final TagKey<Enchantment> enchantingExclusive = tag("blaze_enchanter/enchanting_exclusive");
        public final TagKey<Enchantment> superEnchanting = tag("blaze_enchanter/super_enchanting");
        public final TagKey<Enchantment> superEnchantingExclusive = tag("blaze_enchanter/super_enchanting_exclusive");
        public final TagKey<Enchantment> printingDeny = tag("printer/deny");

        private static TagKey<Enchantment> tag(String path) {
            return TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(CEICommon.ID, path));
        }
    }
}
