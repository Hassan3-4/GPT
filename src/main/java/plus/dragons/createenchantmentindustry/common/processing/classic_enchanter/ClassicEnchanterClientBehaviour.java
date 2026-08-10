/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

/** Client value-box facade backed by the classic enchanter's common filtering behaviour. */
public class ClassicEnchanterClientBehaviour extends FilteringBehaviour<ClassicEnchanterBehaviour> {
    public ClassicEnchanterClientBehaviour(
            ClassicBlazeEnchanterBlockEntity blockEntity, ValueBoxTransform transform) {
        super(blockEntity, transform);
        behaviour = blockEntity.getEnchanterBehaviour();
    }
}
