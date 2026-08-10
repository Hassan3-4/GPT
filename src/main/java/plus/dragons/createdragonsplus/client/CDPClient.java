/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.client;

import net.fabricmc.api.ClientModInitializer;

/** Client entry point for the CEI-focused 26.2 compatibility build. */
public final class CDPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // The CEI-required Fluid Hatch uses Create's normal block rendering and
        // needs no CDP custom-fluid renderer or package partial models.
    }
}
