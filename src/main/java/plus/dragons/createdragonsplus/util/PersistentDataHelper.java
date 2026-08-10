/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.util;

import net.minecraft.nbt.CompoundTag;

/** Helpers for CDP's nested entity-persistent-data namespaces. */
public final class PersistentDataHelper {
    private PersistentDataHelper() {}

    public static CompoundTag getOrCreate(CompoundTag root, String... path) {
        CompoundTag current = root;
        for (String key : path) {
            CompoundTag child = current.getCompound(key).orElse(null);
            if (child == null) {
                child = new CompoundTag();
                current.put(key, child);
            }
            current = child;
        }
        return current;
    }
}
