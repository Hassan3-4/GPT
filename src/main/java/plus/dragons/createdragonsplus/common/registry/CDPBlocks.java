/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.hatch.FluidHatchBlock;

/** CEI-required Fluid Hatch registration for Minecraft 26.2. */
public final class CDPBlocks {
    public static final ModTags MOD_TAGS = new ModTags();
    private static final ResourceKey<Block> FLUID_HATCH_KEY = ResourceKey.create(
            Registries.BLOCK, CDPCommon.asResource("fluid_hatch"));

    public static final FluidHatchBlock FLUID_HATCH = Registry.register(
            BuiltInRegistries.BLOCK,
            FLUID_HATCH_KEY,
            new FluidHatchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected())
                    .mapColor(MapColor.COLOR_GRAY)
                    .setId(FLUID_HATCH_KEY)));

    static {
        var itemKey = ResourceKey.create(Registries.ITEM, CDPCommon.asResource("fluid_hatch"));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(FLUID_HATCH, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
    }

    private CDPBlocks() {}

    public static void register() {
        // Static initialization performs Fabric registry registration.
    }

    public static final class ModTags {
        public final TagKey<Block> passiveBlockFreezers = tag("passive_block_freezers");
        public final TagKey<Block> fanSandingCatalysts = tag("fan_processing_catalysts/sanding");
        public final TagKey<Block> fanEndingCatalysts = tag("fan_processing_catalysts/ending");
        public final TagKey<Block> notApplicablePolishing = tag("not_applicable_for_polishing");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, CDPCommon.asResource(path));
        }
    }
}
