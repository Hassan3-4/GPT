/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.client;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;

/** Client-only sprite and tint registration for CDP's Fabric fluids. */
public final class CDPFluidClient {
    private CDPFluidClient() {}

    public static void register() {
        CDPFluids.DYES_BY_VARIANT.values().forEach(entry ->
                FluidRenderHandlerRegistry.INSTANCE.register(entry.source(), entry.flowing(), new SimpleFluidRenderHandler(
                        CDPCommon.asResource("fluid/dye_still"),
                        CDPCommon.asResource("fluid/dye_flow"),
                        entry.color())));
        FluidRenderHandlerRegistry.INSTANCE.register(
                CDPFluids.DRAGON_BREATH.source(),
                CDPFluids.DRAGON_BREATH.flowing(),
                new SimpleFluidRenderHandler(
                        CDPCommon.asResource("fluid/dragon_breath_still"),
                        CDPCommon.asResource("fluid/dragon_breath_flow"),
                        CDPFluids.DRAGON_BREATH.color()));
    }
}
