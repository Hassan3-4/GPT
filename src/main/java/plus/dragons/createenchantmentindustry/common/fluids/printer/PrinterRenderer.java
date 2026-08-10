package plus.dragons.createenchantmentindustry.common.fluids.printer;

import static plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity.PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;

/** Client renderer using Create Fly's 1.21.11 render-state API. */
public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity, PrinterRenderer.PrinterRenderState> {
    private static final int PISTON_MOVING_TIME = 5;
    private static final PartialModel[] NOZZLE = {
        CEIPartialModels.PRINTER_NOZZLE_TOP,
        CEIPartialModels.PRINTER_NOZZLE_BOTTOM
    };

    public PrinterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PrinterRenderState createRenderState() {
        return new PrinterRenderState();
    }

    @Override
    public void extractRenderState(
        PrinterBlockEntity printer,
        PrinterRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(printer, state, tickProgress, cameraPos, crumblingOverlay);
        if (printer.isRemoved() || printer.tank == null) return;

        TankSegment tank = printer.tank.getPrimaryTank();
        FluidStack fluidStack = tank.getRenderedFluid();
        float fluidLevel = tank.getFluidLevel().getValue(tickProgress);
        if (!fluidStack.isEmpty() && fluidLevel != 0) {
            float height = Math.max(fluidLevel, 0.175f) * (11 / 16f);
            float min = 2.5f / 16f;
            state.fluid = new FluidRenderState(
                fluidStack.getFluid(), fluidStack.getComponentChanges(), min, min, min, min + 11 / 16f, min + height,
                min + 11 / 16f, state.lightCoords
            );
        } else {
            state.fluid = null;
        }

        float progress = getProgress(printer.processingTicks - tickProgress);
        state.parts = new PartsRenderState(
            CachedBuffers.partial(NOZZLE[0], state.blockState),
            CachedBuffers.partial(NOZZLE[1], state.blockState),
            CachedBuffers.partial(CEIPartialModels.PRINTER_PISTON, state.blockState),
            progress,
            state.lightCoords
        );
    }

    @Override
    public void submit(PrinterRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.parts != null) queue.submitCustomGeometry(matrices, RenderTypes.solidMovingBlock(), state.parts);
        if (state.fluid != null) queue.submitCustomGeometry(matrices, RenderTypes.translucentMovingBlock(), state.fluid);
    }

    public static float getProgress(float partialTicks) {
        if (partialTicks < 0) return 0;
        if (partialTicks < PISTON_MOVING_TIME) return Mth.lerp(partialTicks / PISTON_MOVING_TIME, 0, 1);
        if (partialTicks < PROCESSING_TIME - PISTON_MOVING_TIME) return 1;
        if (partialTicks < PROCESSING_TIME) return Mth.lerp((PROCESSING_TIME - partialTicks) / PISTON_MOVING_TIME, 0, 1);
        return 0;
    }

    public static class PrinterRenderState extends SmartRenderState {
        private FluidRenderState fluid;
        private PartsRenderState parts;
    }

    private record FluidRenderState(
        Fluid fluid, DataComponentPatch changes, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matrices, VertexConsumer vertices) {
            FluidRenderHelper.renderFluidBox(
                fluid, changes, minX, minY, minZ, maxX, maxY, maxZ, vertices, matrices, light, false, true
            );
        }
    }

    private record PartsRenderState(
        SuperByteBuffer topNozzle, SuperByteBuffer bottomNozzle, SuperByteBuffer piston, float progress, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matrices, VertexConsumer vertices) {
            matrices.translate(0, 3 * progress / 32f, 0);
            topNozzle.light(light).renderInto(matrices, vertices);
            matrices.translate(0, 3 * progress / 32f, 0);
            bottomNozzle.light(light).renderInto(matrices, vertices);
            matrices.translate(0, -6 * progress / 32f - progress / 2f, 0);
            piston.light(light).renderInto(matrices, vertices);
        }
    }
}
