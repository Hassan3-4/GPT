/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.config;

import com.zurrtum.create.catnip.config.Builder;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** CEI configuration backed by Create Fly's Fabric JSON configuration service. */
public final class CEIConfig {
    private static CEICommonConfig commonConfig;
    private static CEIClientConfig clientConfig;
    private static CEIServerConfig serverConfig;

    private CEIConfig() {}

    public static void register() {
        if (commonConfig != null) return;
        commonConfig = Builder.create(CEICommonConfig::new, CEICommon.ID, "common");
        clientConfig = Builder.create(CEIClientConfig::new, CEICommon.ID, "client");
        serverConfig = Builder.create(CEIServerConfig::new, CEICommon.ID, "server");
    }

    public static CEICommonConfig common() { return commonConfig; }
    public static CEIClientConfig client() { return clientConfig; }
    public static CEIServerConfig server() { return serverConfig; }
    public static CEIKineticsConfig kinetics() { return serverConfig.kinetics; }
    public static CEIStressConfig stress() { return serverConfig.kinetics.stressValues; }
    public static CEIFluidsConfig fluids() { return serverConfig.fluids; }
    public static CEIEnchantmentsConfig enchantments() { return serverConfig.enchantments; }
    public static CEIProcessingConfig processing() { return serverConfig.processing; }
    public static CEIFeaturesConfig features() { return commonConfig.features; }
}
