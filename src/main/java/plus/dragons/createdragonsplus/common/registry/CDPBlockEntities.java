/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.hatch.FluidHatchBlockEntity;

/** Fabric registry for CDP block entities. */
public final class CDPBlockEntities {
    public static final BlockEntityType<FluidHatchBlockEntity> FLUID_HATCH = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CDPCommon.asResource("fluid_hatch"),
            FabricBlockEntityTypeBuilder.create(FluidHatchBlockEntity::new, CDPBlocks.FLUID_HATCH).build());

    private CDPBlockEntities() {}

    public static void register() {
        // Static initialization performs Fabric registry registration.
    }
}
