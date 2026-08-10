package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerArmInteractionPoint;

/** Mechanical-arm targets registered in Create Fly's native extension registry. */
public final class CEIArmInterationPoints {
    public static final BlazeEnchanterArmInteractionPoint.Type BLAZE_ENCHANTER = register(
        "blaze_enchanter", new BlazeEnchanterArmInteractionPoint.Type()
    );
    public static final BlazeForgerArmInteractionPoint.Type BLAZE_FORGER = register(
        "blaze_forger", new BlazeForgerArmInteractionPoint.Type()
    );
    public static final ClassicBlazeEnchanterArmInteractionPoint.Type CLASSIC_BLAZE_ENCHANTER = register(
        "classic_blaze_enchanter", new ClassicBlazeEnchanterArmInteractionPoint.Type()
    );

    private CEIArmInterationPoints() {}

    private static <T extends ArmInteractionPointType> T register(String id, T type) {
        return Registry.register(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CEICommon.asResource(id), type);
    }

    public static void register() {
        // Registrations are performed by the static fields before registries freeze.
    }
}
