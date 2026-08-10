/*
 * Copyright (C) 2025 DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package plus.dragons.createdragonsplus.common.registry;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.content.fluids.AllFlowCollision;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragondBreathLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonsBreathOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariant;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.config.CDPConfig;

/**
 * Fabric registrations for every CDP fluid.
 *
 * <p>Create Fly exposes its native {@link FlowableFluid} implementation on
 * Fabric.  CDP uses it directly, retaining real source/flowing fluids, liquid
 * blocks, buckets, pipe effects and Create pipe collision results instead of
 * replacing the NeoForge fluids with item-only stand-ins.</p>
 */
public final class CDPFluids {
    public static final ModTags MOD_TAGS = new ModTags();
    public static final CommonTags COMMON_TAGS = new CommonTags();
    public static final Map<Identifier, Entry> DYES_BY_VARIANT = new LinkedHashMap<>();
    public static final Entry DRAGON_BREATH = registerDragonBreath();

    static {
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            if (variant.isAvailable())
                DYES_BY_VARIANT.put(variant.id(), registerDye(variant));
        }
    }

    private CDPFluids() {}

    public static void register() {
        registerPipeEffects();
        registerPipeCollisions();
        registerDispenserBehavior();
    }

    public static void registerDispenserBehavior() {
        DYES_BY_VARIANT.values().forEach(entry ->
                net.minecraft.world.level.block.DispenserBlock.registerBehavior(entry.bucket(), StandardDispenserBehaviour.INSTANCE));
        net.minecraft.world.level.block.DispenserBlock.registerBehavior(DRAGON_BREATH.bucket(), StandardDispenserBehaviour.INSTANCE);
    }

    private static Entry registerDye(DyeVariant variant) {
        String name = variant.fluidName();
        int color = 0xFF000000 | variant.color();
        return register(name,
                (entry, properties) -> new DyeLiquidBlock(variant, entry.source(), properties),
                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET),
                color,
                false);
    }

    private static Entry registerDragonBreath() {
        return register("dragon_breath",
                (entry, properties) -> new DragondBreathLiquidBlock(entry.source(), properties),
                BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).lightLevel(state -> 15),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).rarity(Rarity.UNCOMMON),
                0xFFFFFFFF,
                true);
    }

    private static Entry register(
            String path,
            java.util.function.BiFunction<Entry, BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties blockProperties,
            Item.Properties bucketProperties,
            int color,
            boolean dragonBreath) {
        Identifier id = CDPCommon.asResource(path);
        FluidEntry backing = new FluidEntry();
        backing.still = dragonBreath ? new DragonBreathStill(backing) : new FlowableFluid.Still(backing);
        backing.flowing = dragonBreath ? new DragonBreathFlowing(backing) : new FlowableFluid.Flowing(backing);

        Registry.register(BuiltInRegistries.FLUID, ResourceKey.create(Registries.FLUID, id), backing.still);
        Registry.register(BuiltInRegistries.FLUID, ResourceKey.create(Registries.FLUID, id.withPrefix("flowing_")), backing.flowing);

        Entry entry = new Entry(backing, color, dragonBreath);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = blockFactory.apply(entry, blockProperties.setId(blockKey));
        if (!(block instanceof com.zurrtum.create.infrastructure.fluids.FluidBlock fluidBlock)) {
            throw new IllegalStateException("CDP fluid block must use Create Fly's FluidBlock base class");
        }
        backing.block = fluidBlock;
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> bucketKey = ResourceKey.create(Registries.ITEM, id.withSuffix("_bucket"));
        backing.bucket = Registry.register(BuiltInRegistries.ITEM, bucketKey,
                new BucketItem(backing.still, bucketProperties.setId(bucketKey)));
        return entry;
    }

    private static void registerPipeEffects() {
        DYES_BY_VARIANT.forEach((id, entry) -> DyeVariantRegistry.get(id).ifPresent(variant ->
                OpenPipeEffectHandler.REGISTRY.register(entry.source(), new DyeFluidOpenPipeEffect(variant))));
        OpenPipeEffectHandler.REGISTRY.register(DRAGON_BREATH.source(), new DragonsBreathOpenPipeEffect());
    }

    /** Preserve all Create pipe reactions formerly supplied through NeoForge's fluid event hooks. */
    private static void registerPipeCollisions() {
        boolean colouredConcrete = CDPConfig.features().dyeFluidsLavaInteractionGenerateColoredConcrete.get();
        DYES_BY_VARIANT.forEach((id, entry) -> {
            DyeVariant variant = DyeVariantRegistry.get(id).orElseThrow();
            Block concrete = BuiltInRegistries.BLOCK.getValue(variant.concreteBlockId());
            BlockState result = colouredConcrete && concrete != Blocks.AIR
                    ? concrete.defaultBlockState()
                    : Blocks.COBBLESTONE.defaultBlockState();
            addLavaCollision(entry.source(), result);
        });
        addLavaCollision(DRAGON_BREATH.source(), Blocks.END_STONE.defaultBlockState());
    }

    private static void addLavaCollision(Fluid fluid, BlockState result) {
        AllFlowCollision.Flow.put(new AllFlowCollision.FlowEntry(Fluids.LAVA, fluid), result);
        AllFlowCollision.Spill.put(new AllFlowCollision.SpillEntry(Fluids.LAVA, fluid), result);
        AllFlowCollision.Spill.put(new AllFlowCollision.SpillEntry(Fluids.FLOWING_LAVA, fluid), result);
        AllFlowCollision.Spill.put(new AllFlowCollision.SpillEntry(fluid, Fluids.LAVA), result);
        AllFlowCollision.Spill.put(new AllFlowCollision.SpillEntry(fluid, Fluids.FLOWING_LAVA), result);
    }

    public static final class Entry {
        private final FluidEntry backing;
        private final int color;
        private final boolean dragonBreath;

        private Entry(FluidEntry backing, int color, boolean dragonBreath) {
            this.backing = backing;
            this.color = color;
            this.dragonBreath = dragonBreath;
        }

        public FlowableFluid get() {
            return backing.still;
        }

        public FlowableFluid source() {
            return backing.still;
        }

        public FlowableFluid getSource() {
            return source();
        }

        public FlowableFluid flowing() {
            return backing.flowing;
        }

        public BucketItem bucket() {
            return backing.bucket;
        }

        public Optional<BucketItem> getBucket() {
            return Optional.of(bucket());
        }

        public int color() {
            return color;
        }

        public boolean isDragonBreath() {
            return dragonBreath;
        }
    }

    public static final class ModTags {
        public final TagKey<Fluid> fanEndingCatalysts = tag(CDPCommon.ID, "fan_processing_catalysts/ending");
    }

    public static final class CommonTags {
        public final TagKey<Fluid> dyes = tag("c", "dyes");
        public final Map<Identifier, TagKey<Fluid>> dyesByVariant = new LinkedHashMap<>();
        public final TagKey<Fluid> dragonBreath = tag("c", "dragon_breath");

        private CommonTags() {
            for (DyeVariant variant : DyeVariantRegistry.all())
                dyesByVariant.put(variant.id(), tag("c", "dyes/" + variant.serializedName()));
        }
    }

    private static TagKey<Fluid> tag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static final class DragonBreathStill extends FlowableFluid.Still {
        private DragonBreathStill(FluidEntry entry) {
            super(entry);
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 30;
        }

        @Override
        public int getSlopeFindDistance(LevelReader level) {
            return 2;
        }
    }

    private static final class DragonBreathFlowing extends FlowableFluid.Flowing {
        private DragonBreathFlowing(FluidEntry entry) {
            super(entry);
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 30;
        }

        @Override
        public int getSlopeFindDistance(LevelReader level) {
            return 2;
        }
    }
}
