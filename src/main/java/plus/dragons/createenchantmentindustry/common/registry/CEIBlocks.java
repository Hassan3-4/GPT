/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.content.materials.ExperienceBlock;
import com.zurrtum.create.content.processing.AssemblyOperatorBlockItem;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMovementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlock;
import plus.dragons.createenchantmentindustry.platform.BlockReference;

/** CEI's Fabric block and block-item registrations. */
public final class CEIBlocks {
    public static final BlockReference<MechanicalGrindstoneBlock> MECHANICAL_GRINDSTONE = register(
        "mechanical_grindstone", MechanicalGrindstoneBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE),
        MechanicalGrindStoneItem::new, false
    );
    public static final BlockReference<GrindstoneDrainBlock> GRINDSTONE_DRAIN = register(
        "grindstone_drain", properties -> new GrindstoneDrainBlock(MECHANICAL_GRINDSTONE.get(), properties),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK), BlockItem::new, false
    );
    public static final BlockReference<ExperienceHatchBlock> EXPERIENCE_HATCH = register(
        "experience_hatch", ExperienceHatchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).lightLevel(state -> 12), BlockItem::new, false
    );
    public static final BlockReference<PrinterBlock> PRINTER = register(
        "printer", PrinterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK), AssemblyOperatorBlockItem::new, false
    );
    public static final BlockReference<BlazeEnchanterBlock> BLAZE_ENCHANTER = registerBlaze("blaze_enchanter", BlazeEnchanterBlock::new);
    public static final BlockReference<BlazeForgerBlock> BLAZE_FORGER = registerBlaze("blaze_forger", BlazeForgerBlock::new);
    public static final BlockReference<ClassicBlazeEnchanterBlock> CLASSIC_BLAZE_ENCHANTER = registerBlaze(
        "classic_blaze_enchanter", ClassicBlazeEnchanterBlock::new
    );
    public static final BlockReference<ExperienceBlock> SUPER_EXPERIENCE_BLOCK = register(
        "super_experience_block", ExperienceBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(ExperienceBlock.SOUND).requiresCorrectToolForDrops()
            .lightLevel(state -> 15),
        BlockItem::new, true
    );
    public static final BlockReference<ExperienceLanternBlock> EXPERIENCE_LANTERN = register(
        "experience_lantern", ExperienceLanternBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
            .lightLevel(state -> state.getValue(ExperienceLanternBlock.LIGHT)), BlockItem::new, false
    );

    private CEIBlocks() {}

    private static <T extends BlazeBlock<?>> BlockReference<T> registerBlaze(
        String path, java.util.function.Function<BlockBehaviour.Properties, T> factory
    ) {
        return register(path, factory,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().lightLevel(BlazeBlock::getLight),
            BlockItem::new, false);
    }

    private static <T extends Block> BlockReference<T> register(
        String path,
        java.util.function.Function<BlockBehaviour.Properties, T> factory,
        BlockBehaviour.Properties properties,
        java.util.function.BiFunction<T, Item.Properties, ? extends BlockItem> itemFactory,
        boolean rare
    ) {
        Identifier id = CEICommon.asResource(path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        T block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties().useBlockDescriptionPrefix().setId(itemKey);
        if (rare) itemProperties.rarity(Rarity.RARE);
        Registry.register(BuiltInRegistries.ITEM, itemKey, itemFactory.apply(block, itemProperties));
        return new BlockReference<>(BuiltInRegistries.BLOCK, id);
    }

    public static void register() {
        MovementBehaviour.REGISTRY.register(EXPERIENCE_LANTERN.get(), new ExperienceLanternMovementBehaviour());
    }
}
