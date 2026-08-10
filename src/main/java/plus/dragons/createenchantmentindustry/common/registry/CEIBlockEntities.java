/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.platform.BlockEntityReference;

/** Direct vanilla block-entity registrations.  Fluid/item access is supplied
 * by CEI's Fabric inventory providers rather than NeoForge capabilities. */
public final class CEIBlockEntities {
    public static final BlockEntityReference<KineticBlockEntity> MECHANICAL_GRINDSTONE = register(
        "mechanical_grindstone", (pos, state) -> new KineticBlockEntity(type("mechanical_grindstone"), pos, state), CEIBlocks.MECHANICAL_GRINDSTONE.get()
    );
    public static final BlockEntityReference<GrindstoneDrainBlockEntity> GRINDSTONE_DRAIN = register(
        "grindstone_drain", (pos, state) -> new GrindstoneDrainBlockEntity(type("grindstone_drain"), pos, state), CEIBlocks.GRINDSTONE_DRAIN.get()
    );
    public static final BlockEntityReference<ExperienceHatchBlockEntity> EXPERIENCE_HATCH = register(
        "experience_hatch", (pos, state) -> new ExperienceHatchBlockEntity(type("experience_hatch"), pos, state), CEIBlocks.EXPERIENCE_HATCH.get()
    );
    public static final BlockEntityReference<PrinterBlockEntity> PRINTER = register(
        "printer", (pos, state) -> new PrinterBlockEntity(type("printer"), pos, state), CEIBlocks.PRINTER.get()
    );
    public static final BlockEntityReference<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = register(
        "blaze_enchanter", (pos, state) -> new BlazeEnchanterBlockEntity(type("blaze_enchanter"), pos, state), CEIBlocks.BLAZE_ENCHANTER.get()
    );
    public static final BlockEntityReference<BlazeForgerBlockEntity> BLAZE_FORGER = register(
        "blaze_forger", (pos, state) -> new BlazeForgerBlockEntity(type("blaze_forger"), pos, state), CEIBlocks.BLAZE_FORGER.get()
    );
    public static final BlockEntityReference<ClassicBlazeEnchanterBlockEntity> CLASSIC_BLAZE_ENCHANTER = register(
        "classic_blaze_enchanter", (pos, state) -> new ClassicBlazeEnchanterBlockEntity(type("classic_blaze_enchanter"), pos, state), CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get()
    );
    public static final BlockEntityReference<ExperienceLanternBlockEntity> EXPERIENCE_LANTERN = register(
        "experience_lantern", (pos, state) -> new ExperienceLanternBlockEntity(type("experience_lantern"), pos, state), CEIBlocks.EXPERIENCE_LANTERN.get()
    );

    private CEIBlockEntities() {}

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityType<T> type(String path) {
        return (BlockEntityType<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(CEICommon.asResource(path));
    }

    private static <T extends BlockEntity> BlockEntityReference<T> register(
        String path, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block block
    ) {
        Identifier id = CEICommon.asResource(path);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
            FabricBlockEntityTypeBuilder.<T>create(factory, block).build());
        return new BlockEntityReference<>(BuiltInRegistries.BLOCK_ENTITY_TYPE, id);
    }

    public static void register() {}
}
