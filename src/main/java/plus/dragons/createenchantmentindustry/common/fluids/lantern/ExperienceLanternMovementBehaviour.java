package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.fluids.FluidUnits;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

/** Keeps all experience-lantern functions active while the block is mounted. */
public class ExperienceLanternMovementBehaviour extends MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide()) return;
        AABB area = new AABB(context.position.subtract(.5, .5, .5), context.position.add(.5, .5, .5)).inflate(.5);
        if (context.world.getGameTime() % 10 == 0) drainExp(context.world, area, context.contraption.getStorage().getFluids());
        if (CEIConfig.fluids().experienceLanternPullToggle.get()) pullExp(context.world, area, context.position);
    }

    protected void drainExp(Level level, AABB area, MountedFluidStorageWrapper storage) {
        int rate = CEIConfig.fluids().experienceLanternDrainRate.get();
        List<Player> players = level.getEntitiesOfClass(Player.class, area, player -> player.isAlive() && !player.isSpectator());
        int requested = 0;
        for (Player player : players) requested += Math.min(ExperienceHelper.getExperienceForPlayer(player), rate);
        int insertedUnits = storage.insert(new FluidStack(
            CEIFluids.EXPERIENCE,
            FluidUnits.fromMillibuckets(requested)
        ));
        int inserted = FluidUnits.toMillibuckets(insertedUnits);
        for (Player player : players) {
            if (inserted == 0) break;
            int removed = Math.min(Math.min(ExperienceHelper.getExperienceForPlayer(player), rate), inserted);
            if (removed != 0) {
                player.giveExperiencePoints(-removed);
                inserted -= removed;
            }
        }

        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area)) {
            int amount = orb.getValue();
            int acceptedUnits = storage.insert(new FluidStack(
                CEIFluids.EXPERIENCE,
                FluidUnits.fromMillibuckets(amount)
            ));
            int accepted = FluidUnits.toMillibuckets(acceptedUnits);
            if (accepted == amount) {
                orb.remove(Entity.RemovalReason.DISCARDED);
            } else {
                if (accepted != 0) {
                    orb.discard();
                    level.addFreshEntity(new ExperienceOrb(level, orb.getX(), orb.getY(), orb.getZ(), amount - accepted));
                }
                break;
            }
        }
    }

    protected void pullExp(Level level, AABB area, Vec3 position) {
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class,
                area.inflate(CEIConfig.fluids().experienceLanternPullRadius.get()))) {
            if (orb.getDeltaMovement().length() > .5) continue;
            double distance = orb.position().distanceTo(position);
            if (distance < .001) continue;
            double force = CEIConfig.fluids().experienceLanternPullForceMultiplier.get() / distance;
            orb.push(position.subtract(orb.position()).normalize().scale(force));
        }
    }
}
