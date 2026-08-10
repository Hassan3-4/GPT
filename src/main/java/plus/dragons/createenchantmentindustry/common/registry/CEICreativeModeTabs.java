package plus.dragons.createenchantmentindustry.common.registry;

import static com.zurrtum.create.AllBlocks.EXPERIENCE_BLOCK;
import static com.zurrtum.create.AllItems.EXP_NUGGET;
import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIItems.*;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** CEI's Fabric-visible creative tab, including optional CDP content only when present. */
public final class CEICreativeModeTabs {
    public static final CreativeModeTab BASE = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, CEICommon.asResource("base"), base()
    );

    private CEICreativeModeTabs() {}

    private static CreativeModeTab base() {
        return CreativeModeTab.builder(null, -1)
            .title(Component.translatable("itemGroup." + CEICommon.ID + ".base"))
            .icon(BLAZE_ENCHANTER::asStack)
            .displayItems(CEICreativeModeTabs::buildBaseContents)
            .build();
    }

    private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(MECHANICAL_GRINDSTONE);
        output.accept(EXPERIENCE_HATCH);
        output.accept(EXPERIENCE_LANTERN);
        output.accept(PRINTER);
        output.accept(BLAZE_ENCHANTER);
        output.accept(BLAZE_FORGER);
        if (CEIConfig.features().classicBlazeEnchanter.get()) output.accept(CLASSIC_BLAZE_ENCHANTER);
        output.accept(EXPERIENCE_BLOCK);
        output.accept(SUPER_EXPERIENCE_BLOCK);
        output.accept(EXP_NUGGET);
        output.accept(SUPER_EXPERIENCE_NUGGET);
        output.accept(ENCHANTING_TEMPLATE);
        output.accept(SUPER_ENCHANTING_TEMPLATE);
        BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("create_dragons_plus", "blaze_upgrade_smithing_template"))
            .ifPresent(output::accept);
        if (CEIConfig.features().classicBlazeEnchanter.get()) output.accept(BLAZES_ENCHANTING_HANDBOOK);
        output.accept(EXPERIENCE_CAKE_BASE, TabVisibility.SEARCH_TAB_ONLY);
        output.accept(EXPERIENCE_CAKE);
        output.accept(EXPERIENCE_CAKE_SLICE);
        output.accept(EXPERIENCE_BUCKET);
    }

    public static void register() {
        // Static registration above intentionally occurs during Fabric common initialization.
    }
}
