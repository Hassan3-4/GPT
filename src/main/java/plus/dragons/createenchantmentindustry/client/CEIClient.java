/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.client;

import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.content.logistics.itemHatch.HatchFilterSlot;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import plus.dragons.createenchantmentindustry.client.behaviour.BlazeExperienceTooltipBehaviour;
import plus.dragons.createenchantmentindustry.client.behaviour.ExperienceHatchClientBehaviour;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderPlugin;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicEnchanterClientBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterItemRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterItemRenderer;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterScrollValueBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerRenderer;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

/** Fabric client entrypoint for CEI's models and Ponder integration. */
public class CEIClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // The upstream Registrate declarations put all three blaze-machine
        // cages (and the experience lantern) on cutout_mipped. Without the
        // Fabric equivalents, their transparent OBJ texture pixels are baked
        // into the default solid layer and appear as opaque outer panels.
        BlockRenderLayerMap.putBlocks(
            ChunkSectionLayer.CUTOUT,
            CEIBlocks.BLAZE_ENCHANTER.get(),
            CEIBlocks.BLAZE_FORGER.get(),
            CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get(),
            CEIBlocks.EXPERIENCE_LANTERN.get()
        );
        FluidRenderHandlerRegistry.INSTANCE.register(
            CEIFluids.EXPERIENCE,
            CEIFluids.EXPERIENCE_FLOWING,
            new SimpleFluidRenderHandler(
                CEICommon.asResource("fluid/experience_still"),
                CEICommon.asResource("fluid/experience_flow"),
                0x4CFF45
            )
        );
        BlockRenderLayerMap.putFluids(
            ChunkSectionLayer.TRANSLUCENT,
            CEIFluids.EXPERIENCE,
            CEIFluids.EXPERIENCE_FLOWING
        );
        AllModels.register(BlazeEnchanterItemRenderer.ID, BlazeEnchanterItemRenderer.Unbaked.CODEC);
        AllModels.register(
            ClassicBlazeEnchanterItemRenderer.ID, ClassicBlazeEnchanterItemRenderer.Unbaked.CODEC);
        // CEIPartialModels must be registered here,
        // or when PartialModelEventHandler#onRegisterAdditional triggered,
        // PartialModel.ALL won't include all partial model in 'some cases'
        // AllPartialModels#ini does not do this since AllPartialModels is already triggered at AllBlocks.TRACK
        // Issue: https://github.com/Creators-of-Create/Create/issues/8259
        CEIPartialModels.register();
        SimpleBlockEntityVisualizer.builder(CEIBlockEntities.MECHANICAL_GRINDSTONE.get())
            .factory(SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE))
            .apply();
        SimpleBlockEntityVisualizer.builder(CEIBlockEntities.GRINDSTONE_DRAIN.get())
            .factory(SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE))
            .neverSkipVanillaRender()
            .apply();
        AllBlockEntityRenders.render(CEIBlockEntities.PRINTER.get(), PrinterRenderer::new);
        AllBlockEntityRenders.render(CEIBlockEntities.GRINDSTONE_DRAIN.get(), GrindstoneDrainRenderer::new);
        AllBlockEntityRenders.render(CEIBlockEntities.MECHANICAL_GRINDSTONE.get(), KineticBlockEntityRenderer::new);
        AllBlockEntityRenders.render(CEIBlockEntities.BLAZE_ENCHANTER.get(), BlazeEnchanterRenderer::new);
        AllBlockEntityRenders.render(CEIBlockEntities.BLAZE_FORGER.get(), BlazeForgerRenderer::new);
        AllBlockEntityRenders.render(
            CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(), ClassicBlazeEnchanterRenderer::new);
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.BLAZE_ENCHANTER.get(), BlazeExperienceTooltipBehaviour::new);
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.BLAZE_FORGER.get(), BlazeExperienceTooltipBehaviour::new);
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(), BlazeExperienceTooltipBehaviour::new);
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.PRINTER.get(),
            printer -> new FilteringBehaviour<>(
                printer,
                new CenteredSideValueBoxTransform((state, side) -> state.getValue(PrinterBlock.FACING) == side)
            )
        );
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.EXPERIENCE_HATCH.get(),
            hatch -> new ExperienceHatchClientBehaviour(hatch, new HatchFilterSlot())
        );
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.BLAZE_ENCHANTER.get(),
            enchanter -> new EnchanterScrollValueBehaviour(
                enchanter, new EnchanterScrollValueBehaviour.EnchanterTransform())
        );
        BlockEntityBehaviour.addClient(
            CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(),
            enchanter -> new ClassicEnchanterClientBehaviour(
                enchanter, new ClassicBlazeEnchanterBlockEntity.EnchanterTransform())
        );
        PonderIndex.addPlugin(new CEIPonderPlugin());
    }
}
