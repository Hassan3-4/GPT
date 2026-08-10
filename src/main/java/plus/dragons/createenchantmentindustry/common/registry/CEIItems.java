package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.content.materials.ExperienceNuggetItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.platform.ItemReference;

/** Direct Fabric item registrations. Tags, models and language remain data assets. */
public final class CEIItems {
    public static final ItemReference<ExperienceNuggetItem> SUPER_EXPERIENCE_NUGGET = register("super_experience_nugget",
        properties -> new ExperienceNuggetItem(properties.rarity(Rarity.RARE)));
    public static final ItemReference<EnchantingTemplateItem> ENCHANTING_TEMPLATE = register("enchanting_template",
        properties -> EnchantingTemplateItem.normal(properties.rarity(Rarity.UNCOMMON).component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)));
    public static final ItemReference<EnchantingTemplateItem> SUPER_ENCHANTING_TEMPLATE = register("super_enchanting_template",
        properties -> EnchantingTemplateItem.special(properties.rarity(Rarity.RARE).component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)));
    public static final ItemReference<Item> BLAZES_ENCHANTING_HANDBOOK = register("blazes_enchanting_handbook", Item::new);
    public static final ItemReference<Item> EXPERIENCE_CAKE_BASE = register("experience_cake_base", Item::new);
    public static final ItemReference<Item> EXPERIENCE_CAKE = register("experience_cake",
        properties -> new Item(properties.rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final ItemReference<Item> EXPERIENCE_CAKE_SLICE = register("experience_cake_slice",
        properties -> new Item(properties.rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    /** Registered by CEIFluids with the experience fluid's still form. */
    public static final ItemReference<BucketItem> EXPERIENCE_BUCKET = new ItemReference<>(BuiltInRegistries.ITEM, CEICommon.asResource("experience_bucket"));

    private CEIItems() {}

    private static <T extends Item> ItemReference<T> register(String path, java.util.function.Function<Item.Properties, T> factory) {
        Identifier id = CEICommon.asResource(path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
        return new ItemReference<>(BuiltInRegistries.ITEM, id);
    }

    public static void register() {}
}
