package plus.dragons.createenchantmentindustry.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.advancements.BuiltinTrigger;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

/** CEI-owned advancement definition and data generator, independent of CDP/Registrate. */
public final class CEIAdvancement {
    private static final String SECRET_SUFFIX = "\n§7(Hidden Advancement)";
    private final Advancement.Builder vanilla = Advancement.Builder.advancement();
    private final Builder builder = new Builder();
    private final String id;
    private BuiltinTrigger trigger;
    private CEIAdvancement parent;
    private AdvancementHolder result;
    private String title;
    private String description;

    public CEIAdvancement(String id, UnaryOperator<Builder> setup) {
        this.id = id;
        setup.apply(builder);
        if (!builder.externalTrigger) {
            trigger = CEIAdvancements.BuiltinTriggersQuickDeploy.add(CEICommon.asResource(id));
            vanilla.addCriterion("0", trigger.createCriterion(new BuiltinTrigger.Conditions()));
        }
        if (builder.type == TaskType.SECRET) description += SECRET_SUFFIX;
        CEIAdvancements.ENTRIES.add(this);
    }

    public BuiltinTrigger builtinTrigger() {
        if (trigger == null) throw new UnsupportedOperationException("Advancement " + id + " uses an external trigger");
        return trigger;
    }
    public void awardTo(Player player) { if (player instanceof ServerPlayer server && trigger != null) trigger.trigger(server); }
    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer server)) return true;
        AdvancementHolder advancement = ((net.minecraft.server.level.ServerLevel) server.level()).getServer()
            .getAdvancements().get(CEICommon.asResource(id));
        return advancement == null || server.getAdvancements().getOrStartProgress(advancement).isDone();
    }
    public void save(Consumer<AdvancementHolder> sink, HolderLookup.Provider registries) {
        if (parent != null) vanilla.parent(parent.result);
        if (builder.iconFactory != null) builder.icon(builder.iconFactory.apply(registries));
        vanilla.display(builder.icon, Component.translatable(key()), Component.translatable(key() + ".desc").withStyle(s -> s.withColor(0xDBA213)),
            id.equals("root") ? CEICommon.asResource("gui/advancements") : null,
            builder.type.type, builder.type.toast, builder.type.announce, builder.type.hidden);
        result = vanilla.save(sink, CEICommon.asResource(id).toString());
    }
    public void provideLang(BiConsumer<String, String> out) { out.accept(key(), title); out.accept(key() + ".desc", description); }
    public String id() { return id; }
    public ResourceCondition[] loadConditions() { return builder.loadConditions; }
    private String key() { return "advancement." + CEICommon.ID + "." + id; }

    public enum TaskType {
        SILENT(AdvancementType.TASK, false, false, false), NORMAL(AdvancementType.TASK, true, false, false),
        NOISY(AdvancementType.TASK, true, true, false), EXPERT(AdvancementType.GOAL, true, true, false),
        SECRET(AdvancementType.GOAL, true, true, true);
        final AdvancementType type; final boolean toast, announce, hidden;
        TaskType(AdvancementType type, boolean toast, boolean announce, boolean hidden) { this.type=type; this.toast=toast; this.announce=announce; this.hidden=hidden; }
    }

    public final class Builder {
        private TaskType type = TaskType.NORMAL; private boolean externalTrigger; private int criterionIndex;
        private ItemStack icon = ItemStack.EMPTY; private Function<HolderLookup.Provider, ItemStack> iconFactory;
        private ResourceCondition[] loadConditions = new ResourceCondition[0];
        public Builder special(TaskType type) { this.type=type; return this; }
        public Builder after(CEIAdvancement other) { parent=other; return this; }
        public Builder icon(ItemLike item) { return icon(new ItemStack(item)); }
        public Builder icon(ItemStack stack) { icon=stack; return this; }
        public Builder icon(Function<HolderLookup.Provider, ItemStack> factory) { iconFactory=factory; return this; }
        public Builder title(String value) { title=value; return this; }
        public Builder description(String value) { description=value; return this; }
        public Builder whenBlockPlaced(Block block) { return external(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block)); }
        public Builder whenIconCollected() { return whenItemCollected(icon.getItem()); }
        public Builder whenItemCollected(ItemLike item) { return external(InventoryChangeTrigger.TriggerInstance.hasItems(item)); }
        public Builder whenItemCollected(TagKey<Item> tag) { return external(InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag).build())); }
        public Builder whenStatReach(Stat<?> stat, MinMaxBounds.Ints bounds) { return external(plus.dragons.createenchantmentindustry.common.advancements.StatTrigger.Conditions.of(stat, bounds)); }
        public Builder awardedForFree() { return external(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{})); }
        public Builder loadWhen(ResourceCondition... conditions) { loadConditions = conditions; return this; }
        public Builder external(Criterion<?> criterion) { vanilla.addCriterion(String.valueOf(criterionIndex++), criterion); externalTrigger=true; return this; }
    }
}
