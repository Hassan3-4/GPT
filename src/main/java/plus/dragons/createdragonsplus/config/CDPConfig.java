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

package plus.dragons.createdragonsplus.config;

import com.zurrtum.create.catnip.config.Builder;
import plus.dragons.createdragonsplus.common.CDPCommon;

public class CDPConfig {
    private static CDPCommonConfig commonConfig;
    private static CDPClientConfig clientConfig;
    private static CDPServerConfig serverConfig;

    /**
     * Register the three configuration scopes before any gameplay registration
     * reads a feature flag.  Create Fly's Fabric config implementation persists
     * exactly the same common/client/server split as the original mod.
     */
    public static void register() {
        if (commonConfig != null)
            return;
        commonConfig = Builder.create(CDPCommonConfig::new, CDPCommon.ID, "common");
        clientConfig = Builder.create(CDPClientConfig::new, CDPCommon.ID, "client");
        serverConfig = Builder.create(CDPServerConfig::new, CDPCommon.ID, "server");
    }

    public static CDPCommonConfig common() {
        return commonConfig;
    }

    public static CDPClientConfig client() {
        return clientConfig;
    }

    public static CDPServerConfig server() {
        return serverConfig;
    }

    public static CDPFeaturesConfig features() {
        return commonConfig.features;
    }

    public static CDPRecipesConfig recipes() {
        return serverConfig.recipes;
    }
}
