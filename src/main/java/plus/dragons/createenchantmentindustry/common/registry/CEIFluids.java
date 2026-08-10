/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceBucketItem;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceEffectHandler;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;

/**
 * Liquid Experience's Fabric registration.  Create Fly deliberately uses
 * vanilla fluid objects, so the still form, flowing form, world block and
 * bucket are registered together here rather than through Registrate.
 */
public final class CEIFluids {
    private static final FluidEntry EXPERIENCE_ENTRY = new FluidEntry();

    public static final FlowableFluid.Still EXPERIENCE = new FlowableFluid.Still(EXPERIENCE_ENTRY);
    public static final FlowableFluid.Flowing EXPERIENCE_FLOWING = new FlowableFluid.Flowing(EXPERIENCE_ENTRY);
    public static final FluidBlock EXPERIENCE_BLOCK;
    public static final BucketItem EXPERIENCE_BUCKET;

    static {
        EXPERIENCE_ENTRY.still = EXPERIENCE;
        EXPERIENCE_ENTRY.flowing = EXPERIENCE_FLOWING;

        Registry.register(BuiltInRegistries.FLUID, key(Registries.FLUID, "experience"), EXPERIENCE);
        Registry.register(BuiltInRegistries.FLUID, key(Registries.FLUID, "flowing_experience"), EXPERIENCE_FLOWING);

        EXPERIENCE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, key(Registries.BLOCK, "experience"),
            new FluidBlock(EXPERIENCE, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                .lightLevel(state -> 15)
                .setId(key(Registries.BLOCK, "experience"))));
        EXPERIENCE_ENTRY.block = EXPERIENCE_BLOCK;

        EXPERIENCE_BUCKET = Registry.register(BuiltInRegistries.ITEM, key(Registries.ITEM, "experience_bucket"),
            new ExperienceBucketItem(EXPERIENCE, new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .setId(key(Registries.ITEM, "experience_bucket"))));
        EXPERIENCE_ENTRY.bucket = EXPERIENCE_BUCKET;
    }

    private CEIFluids() {}

    private static <T> ResourceKey<T> key(net.minecraft.resources.ResourceKey<? extends Registry<T>> registry, String path) {
        return ResourceKey.create(registry, CEICommon.asResource(path));
    }

    /** Called by the Fabric entry point after the registries have been created. */
    public static void register() {
        AllFluidItemInventory.ALL.put(EXPERIENCE_BUCKET,
            new AllFluidItemInventory.Entry(BucketFluidInventory::new));
        ExperienceEffectHandler handler = new ExperienceEffectHandler();
        OpenPipeEffectHandler.REGISTRY.register(EXPERIENCE, handler);
        OpenPipeEffectHandler.REGISTRY.register(EXPERIENCE_FLOWING, handler);
        DispenserBlock.registerBehavior(EXPERIENCE_BUCKET, StandardDispenserBehaviour.INSTANCE);
    }
}
