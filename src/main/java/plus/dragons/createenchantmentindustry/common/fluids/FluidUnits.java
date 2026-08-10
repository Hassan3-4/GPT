/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.fluids;

import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;

/**
 * Converts CEI's public/configured millibucket amounts to Create Fly's native
 * fluid units. Create Fly follows Fabric Transfer API's 81,000 units per
 * bucket, while CEI's recipes and configuration use 1,000 mB per bucket.
 */
public final class FluidUnits {
    public static final int PER_MILLIBUCKET = BucketFluidInventory.CAPACITY / 1000;

    private FluidUnits() {}

    public static int fromMillibuckets(int millibuckets) {
        return Math.multiplyExact(millibuckets, PER_MILLIBUCKET);
    }

    public static int toMillibuckets(int units) {
        return units / PER_MILLIBUCKET;
    }
}
