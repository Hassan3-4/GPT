package plus.dragons.createenchantmentindustry.data;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;

/** Generates the block drops for every block CEI registers on Fabric. */
public final class CEIBlockLootTableProvider extends FabricBlockLootTableProvider {
    public CEIBlockLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        dropSelf(MECHANICAL_GRINDSTONE.get());
        dropSelf(GRINDSTONE_DRAIN.get());
        dropSelf(EXPERIENCE_HATCH.get());
        dropSelf(PRINTER.get());
        dropSelf(BLAZE_ENCHANTER.get());
        dropSelf(BLAZE_FORGER.get());
        dropSelf(CLASSIC_BLAZE_ENCHANTER.get());
        dropSelf(SUPER_EXPERIENCE_BLOCK.get());
        dropSelf(EXPERIENCE_LANTERN.get());
    }
}
