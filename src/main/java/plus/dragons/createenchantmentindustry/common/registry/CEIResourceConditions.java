/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.RegistryOps;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Fabric replacement for CEI's former CDP/NeoForge config feature condition. */
public final class CEIResourceConditions {
    public static final ClassicBlazeEnchanterEnabled CLASSIC_BLAZE_ENCHANTER_ENABLED =
            new ClassicBlazeEnchanterEnabled();

    private CEIResourceConditions() {}

    public static void register() {
        ResourceConditions.register(ClassicBlazeEnchanterEnabled.TYPE);
    }

    public static final class ClassicBlazeEnchanterEnabled implements ResourceCondition {
        private static final MapCodec<ClassicBlazeEnchanterEnabled> CODEC =
                MapCodec.of(Encoder.empty(), Decoder.unit(ClassicBlazeEnchanterEnabled::new));
        private static final ResourceConditionType<ClassicBlazeEnchanterEnabled> TYPE =
                ResourceConditionType.create(
                        CEICommon.asResource("classic_blaze_enchanter_enabled"), CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.RegistryInfoLookup registryLookup) {
            return CEIConfig.features().classicBlazeEnchanter.get();
        }
    }
}
