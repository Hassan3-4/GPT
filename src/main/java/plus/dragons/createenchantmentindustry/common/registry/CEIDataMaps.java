/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFuel;
import plus.dragons.createenchantmentindustry.util.CEIIntIntPair;

/**
 * Fabric replacement for NeoForge data maps.
 *
 * <p>The existing {@code data_maps/...} resources remain the public data-pack
 * format.  They are loaded on every server-data reload, support direct ids and
 * tags, and preserve the old {@code neoforge:conditions}/{@code neoforge:value}
 * wrapper for the generated optional integrations.</p>
 */
public final class CEIDataMaps {
    private static final Codec<Integer> FLUID_AMOUNT_CODEC = ExtraCodecs.POSITIVE_INT.xmap(
        FluidUnits::fromMillibuckets,
        FluidUnits::toMillibuckets
    );
    public static final CeiDataMap<Item, ExperienceFuel> EXPERIENCE_FUEL = CeiDataMap.item(
        "experience_fuel", ExperienceFuel.CODEC);
    public static final CeiDataMap<Fluid, Integer> FLUID_UNIT_EXPERIENCE = CeiDataMap.fluid(
        "unit/experience", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_ADDRESS_INGREDIENT = CeiDataMap.fluid(
        "printing/address/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_PATTERN_INGREDIENT = CeiDataMap.fluid(
        "printing/pattern/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_COPY_INGREDIENT = CeiDataMap.fluid(
        "printing/copy/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_CUSTOM_NAME_INGREDIENT = CeiDataMap.fluid(
        "printing/custom_name/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Style> PRINTING_CUSTOM_NAME_STYLE = CeiDataMap.fluid(
        "printing/custom_name/style", Style.Serializer.CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_WRITTEN_BOOK_INGREDIENT = CeiDataMap.fluid(
        "printing/written_book/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Fluid, Integer> PRINTING_BANNER_PATTERN_INGREDIENT = CeiDataMap.fluid(
        "printing/banner_pattern/ingredient", FLUID_AMOUNT_CODEC);
    public static final CeiDataMap<Enchantment, List<CEIIntIntPair>> PRINTING_ENCHANTED_BOOK_COST = CeiDataMap.enchantment(
        "printing/enchanted_book/custom_cost", Codec.list(CEIIntIntPair.CODEC));
    public static final CeiDataMap<Enchantment, Float> FORGING_COST_MULTIPLIER = CeiDataMap.enchantment(
        "forging/cost_multiplier", ExtraCodecs.POSITIVE_FLOAT);
    public static final CeiDataMap<Enchantment, Float> SPLITTING_COST_MULTIPLIER = CeiDataMap.enchantment(
        "forging/split_enchantment_cost_multiplier", ExtraCodecs.POSITIVE_FLOAT);
    public static final CeiDataMap<Enchantment, Integer> SUPER_ENCHANTING_LEVEL_EXTENSION = CeiDataMap.enchantment(
        "super_enchanting/custom_level_extension", ExtraCodecs.NON_NEGATIVE_INT);

    private static final List<CeiDataMap<?, ?>> ALL = List.of(
        EXPERIENCE_FUEL,
        FLUID_UNIT_EXPERIENCE,
        PRINTING_ADDRESS_INGREDIENT,
        PRINTING_PATTERN_INGREDIENT,
        PRINTING_COPY_INGREDIENT,
        PRINTING_CUSTOM_NAME_INGREDIENT,
        PRINTING_CUSTOM_NAME_STYLE,
        PRINTING_WRITTEN_BOOK_INGREDIENT,
        PRINTING_BANNER_PATTERN_INGREDIENT,
        PRINTING_ENCHANTED_BOOK_COST,
        FORGING_COST_MULTIPLIER,
        SPLITTING_COST_MULTIPLIER,
        SUPER_ENCHANTING_LEVEL_EXTENSION
    );

    private CEIDataMaps() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return CEICommon.asResource("data_maps");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ALL.forEach(map -> map.reload(resourceManager));
            }
        });
    }

    /** Returns the still-fluid entries covered by a map, including tag values. */
    public static <T> Stream<Pair<Fluid, T>> getSourceFluidEntries(CeiDataMap<Fluid, T> type) {
        return BuiltInRegistries.FLUID.stream()
            .filter(fluid -> FluidHelper.convertToStill(fluid) == fluid)
            .map(fluid -> Pair.of(fluid, type.get(fluid)))
            .filter(entry -> entry.getSecond() != null);
    }
}
