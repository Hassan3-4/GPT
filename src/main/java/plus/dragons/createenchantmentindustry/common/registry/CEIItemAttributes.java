package plus.dragons.createenchantmentindustry.common.registry;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneHelper;

/** Item-filter attribute used by mechanical arms and filters for the grindstone. */
public final class CEIItemAttributes {
    public static final ItemAttributeType PROCESSABLE_BY_MECHANICAL_GRINDSTONE = Registry.register(
        CreateRegistries.ITEM_ATTRIBUTE_TYPE,
        CEICommon.asResource("processable_by_mechanical_grindstone"),
        new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(
            type,
            CEIItemAttributes::isProcessableByMechanicalGrindstone,
            CEICommon.ID + ".processable_by_mechanical_grindstone"
        ))
    );

    private CEIItemAttributes() {}

    private static boolean isProcessableByMechanicalGrindstone(ItemStack stack, net.minecraft.world.level.Level level) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            if (serverLevel.recipeAccess().getRecipeFor(CEIRecipes.GRINDING.getType(), input, level).isPresent()) return true;
            if (serverLevel.recipeAccess().getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING, input, level).isPresent()) return true;
        }
        return GrindstoneHelper.canItemBeGrinded(stack, ItemStack.EMPTY);
    }

    public static void register() {
        // Static initialisation performs the Create Fly registry insertion.
    }
}
