/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Reloadable direct-id and tag-id map used in place of NeoForge DataMapType. */
public final class CeiDataMap<T, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CEICommon.ID + "/data_maps");

    private final String path;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final @Nullable Registry<T> registry;
    private final Codec<V> codec;
    private volatile Map<Identifier, V> direct = Map.of();
    private volatile Map<TagKey<T>, V> tagged = Map.of();

    private CeiDataMap(String path, ResourceKey<? extends Registry<T>> registryKey, @Nullable Registry<T> registry, Codec<V> codec) {
        this.path = path;
        this.registryKey = registryKey;
        this.registry = registry;
        this.codec = codec;
    }

    public static <V> CeiDataMap<Item, V> item(String path, Codec<V> codec) {
        return new CeiDataMap<>(path, Registries.ITEM, BuiltInRegistries.ITEM, codec);
    }

    public static <V> CeiDataMap<Fluid, V> fluid(String path, Codec<V> codec) {
        return new CeiDataMap<>(path, Registries.FLUID, BuiltInRegistries.FLUID, codec);
    }

    public static <V> CeiDataMap<Enchantment, V> enchantment(String path, Codec<V> codec) {
        // Enchantments are a dynamic registry in 1.21.11. Values are therefore
        // looked up from their Holder at use time instead of a built-in registry.
        return new CeiDataMap<>(path, Registries.ENCHANTMENT, null, codec);
    }

    public @Nullable V get(T value) {
        if (registry == null) {
            return null;
        }
        return get(registry.wrapAsHolder(value));
    }

    public @Nullable V get(Holder<T> holder) {
        V directValue = holder.unwrapKey()
            .map(ResourceKey::identifier)
            .map(direct::get)
            .orElse(null);
        if (directValue != null) {
            return directValue;
        }
        for (Map.Entry<TagKey<T>, V> entry : tagged.entrySet()) {
            if (holder.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    void reload(ResourceManager resourceManager) {
        Map<Identifier, V> directValues = new LinkedHashMap<>();
        Map<TagKey<T>, V> tagValues = new LinkedHashMap<>();
        Identifier resourceId = CEICommon.asResource("data_maps/" + registryPath() + "/" + path + ".json");

        for (Resource resource : resourceManager.getResourceStack(resourceId)) {
            try (var reader = resource.openAsReader()) {
                JsonElement document = JsonParser.parseReader(reader);
                if (!document.isJsonObject() || !document.getAsJsonObject().has("values")) {
                    LOGGER.warn("Ignoring malformed CEI data map {} from {}", resourceId, resource.sourcePackId());
                    continue;
                }
                for (Map.Entry<String, JsonElement> entry : document.getAsJsonObject().getAsJsonObject("values").entrySet()) {
                    V value = decode(entry.getValue(), resourceId, entry.getKey());
                    if (value == null) {
                        continue;
                    }
                    if (entry.getKey().startsWith("#")) {
                        Identifier tagId = Identifier.tryParse(entry.getKey().substring(1));
                        if (tagId == null) {
                            LOGGER.warn("Ignoring invalid data-map tag {} in {}", entry.getKey(), resourceId);
                            continue;
                        }
                        tagValues.put(TagKey.create(registryKey, tagId), value);
                    } else {
                        Identifier id = Identifier.tryParse(entry.getKey());
                        if (id == null) {
                            LOGGER.warn("Ignoring invalid data-map id {} in {}", entry.getKey(), resourceId);
                            continue;
                        }
                        directValues.put(id, value);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.error("Failed to load CEI data map {} from {}", resourceId, resource.sourcePackId(), exception);
            }
        }

        direct = Map.copyOf(directValues);
        tagged = Map.copyOf(tagValues);
    }

    private @Nullable V decode(JsonElement element, Identifier resourceId, String entryId) {
        JsonElement value = unwrapConditionalValue(element);
        if (value == null) {
            return null;
        }
        var ops = RegistryOps.create(JsonOps.INSTANCE,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        return codec.parse(ops, value).resultOrPartial(error ->
            LOGGER.error("Failed to decode CEI data-map entry {} in {}: {}", entryId, resourceId, error)
        ).orElse(null);
    }

    private static @Nullable JsonElement unwrapConditionalValue(JsonElement element) {
        if (!element.isJsonObject()) {
            return element;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("neoforge:conditions") && !conditionsMet(object.get("neoforge:conditions"))) {
            return null;
        }
        return object.has("neoforge:value") ? object.get("neoforge:value") : element;
    }

    private static boolean conditionsMet(JsonElement conditions) {
        if (!conditions.isJsonArray()) {
            return false;
        }
        for (JsonElement condition : conditions.getAsJsonArray()) {
            if (!condition.isJsonObject()) {
                return false;
            }
            JsonObject object = condition.getAsJsonObject();
            String type = object.has("type") ? object.get("type").getAsString() : "";
            if (type.endsWith(":mod_loaded") && object.has("modid")) {
                if (!FabricLoader.getInstance().isModLoaded(object.get("modid").getAsString())) {
                    return false;
                }
            } else {
                // Data packs must not silently activate a condition Fabric does
                // not understand, matching NeoForge's conservative behaviour.
                return false;
            }
        }
        return true;
    }

    private String registryPath() {
        Identifier key = registryKey.identifier();
        return key.getPath();
    }
}
