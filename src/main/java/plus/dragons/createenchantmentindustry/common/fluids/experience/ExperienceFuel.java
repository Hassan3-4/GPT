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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public record ExperienceFuel(int experience, boolean special, Optional<ItemStack> usingConvertTo) {

    public static final Codec<ExperienceFuel> INLINE_CODEC = ExtraCodecs.POSITIVE_INT.flatComapMap(
            ExperienceFuel::normal,
            fuel -> !fuel.special && fuel.usingConvertTo.isEmpty()
                    ? DataResult.success(fuel.experience())
                    : DataResult.error(() -> "ExperienceFuel " + fuel + " can not be encoded inline"));
    public static final Codec<ExperienceFuel> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("experience").forGetter(ExperienceFuel::experience),
            Codec.BOOL.optionalFieldOf("special", false).forGetter(ExperienceFuel::special),
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("using_convert_to").forGetter(ExperienceFuel::usingConvertTo)).apply(instance, ExperienceFuel::new));
    /** Accept the compact integer form first while retaining full fuel metadata. */
    public static final Codec<ExperienceFuel> CODEC = Codec.withAlternative(INLINE_CODEC, FULL_CODEC);
    public static ExperienceFuel normal(int experience) {
        return new ExperienceFuel(experience, false, Optional.empty());
    }

    public static ExperienceFuel normal(int experience, ItemStack usingConvertTo) {
        return new ExperienceFuel(experience, false, Optional.of(usingConvertTo));
    }

    public static ExperienceFuel special(int experience) {
        return new ExperienceFuel(experience, true, Optional.empty());
    }

    public static ExperienceFuel special(int experience, ItemStack usingConvertTo) {
        return new ExperienceFuel(experience, true, Optional.of(usingConvertTo));
    }

    public static @Nullable ExperienceFuel get(Level level, ItemStack stack) {
        var fuel = CEIDataMaps.EXPERIENCE_FUEL.get(stack.getItem());
        if (fuel != null)
            return fuel;
        fuel = getBuiltinFuel(stack);
        if (fuel != null)
            return fuel;
        if (!GenericItemEmptying.canItemBeEmptied(level, stack))
            return null;
        var emptying = GenericItemEmptying.emptyItem(level, stack, true);
        var fluid = emptying.getFirst();
        var experience = ExperienceHelper.getExperienceFromFluid(fluid);
        if (experience == 0)
            return null;
        var item = emptying.getSecond();
        return normal(experience, item);
    }

    /**
     * NeoForge data maps are synchronized to clients. The Fabric resource reload
     * replacement is server-data only, so built-in fuels need a deterministic
     * fallback for client-side interaction prediction. Data-pack values still win.
     */
    private static @Nullable ExperienceFuel getBuiltinFuel(ItemStack stack) {
        if (stack.is(CEIItems.EXPERIENCE_CAKE.get()))
            return special(1000);
        if (stack.is(CEIItems.EXPERIENCE_CAKE_SLICE.get()))
            return special(250);
        if (stack.is(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get().asItem()))
            return special(27);
        if (stack.is(CEIItems.SUPER_EXPERIENCE_NUGGET.get()))
            return special(3);
        if (stack.is(AllItems.EXPERIENCE_BLOCK))
            return normal(27);
        if (stack.is(AllItems.EXP_NUGGET))
            return normal(3);
        if (stack.is(CEIItems.EXPERIENCE_BUCKET.get()))
            return normal(1000, new ItemStack(Items.BUCKET));
        return null;
    }
}
