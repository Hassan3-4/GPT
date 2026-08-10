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

package plus.dragons.createdragonsplus.integration;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import plus.dragons.createdragonsplus.platform.RegistryReference;
import plus.dragons.createdragonsplus.platform.create.StandardProcessingRecipe;

public enum ModIntegration {
    //TODO: Keep an eye on Create Garnished 2. Wait it add back Fan Processing
    CREATE_GARNISHED(Constants.CREATE_GARNISHED),
    CREATE_DND(Constants.CREATE_DND),
    IMMERSIVE_ENGINEERING(Constants.IMMERSIVE_ENGINEERING),
    QUICKSAND(Constants.QUICKSAND),
    DYE_DEPOT(Constants.DYE_DEPOT),
    DYENAMICS(Constants.DYENAMICS),
    ARTS_AND_CRAFTS(Constants.ARTS_AND_CRAFTS),
    AETHER(Constants.AETHER),
    SABLE(Constants.SABLE),;

    private final String id;

    ModIntegration(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(id, path);
    }

    public java.util.function.BooleanSupplier condition() {
        return this::enabled;
    }

    public static class Constants {
        public static final String CREATE_GARNISHED = "garnished";
        public static final String CREATE_DND = "dndesires";
        public static final String IMMERSIVE_ENGINEERING = "immersiveengineering";
        public static final String QUICKSAND = "quicksand";
        public static final String DYE_DEPOT = "dye_depot";
        public static final String DYENAMICS = "dyenamics";
        public static final String ARTS_AND_CRAFTS = "arts_and_crafts";
        public static final String AETHER = "aether";
        public static final String SABLE = "sable";
    }

    public RegistryReference<FanProcessingType> fanType(String path) {
        return new RegistryReference<>(CreateRegistries.FAN_PROCESSING_TYPE, asResource(path));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public RegistryReference<RecipeType<StandardProcessingRecipe<SingleRecipeInput>>> recipeType(String path) {
        return new RegistryReference((net.minecraft.core.Registry) BuiltInRegistries.RECIPE_TYPE, asResource(path));
    }
}
