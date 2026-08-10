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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.platform.create.ProcessingRecipe;
import plus.dragons.createenchantmentindustry.platform.create.ProcessingRecipeBuilder;

public class PrintingRecipe extends ProcessingRecipe<PrintingInput, PrintingRecipeParams> {
    public PrintingRecipe(PrintingRecipeParams params) {
        super(CEIRecipes.PRINTING, params);
    }

    public static Builder builder(Identifier id, PlaySoundEffect sound) {
        return new Builder(id, sound);
    }

    public static Builder builder(Identifier id) {
        return new Builder(id, new PlaySoundEffect(List.of(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENCHANTMENT_TABLE_USE)),
                ConstantFloat.of(1),
                UniformFloat.of(.9f, 1f)));
    }

    public void playSound(Level level, BlockPos pos, SoundSource source) {
        var sound = this.params.sound;
        level.playSound(null,
                pos,
                sound.soundEvents().getFirst().value(),
                source,
                sound.volume().sample(level.random),
                sound.pitch().sample(level.random));
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(PrintingInput input, Level level) {
        return ingredients.get(0).test(input.base()) &&
                ingredients.get(1).test(input.template()) &&
                (input.fluid().isEmpty() || fluidIngredients.getFirst().test(input.fluid()));
    }

    public static class Builder extends ProcessingRecipeBuilder<PrintingRecipeParams, PrintingRecipe, Builder> {
        protected Builder(Identifier id, PlaySoundEffect sound) {
            super(PrintingRecipe::new, id);
            PrintingRecipeParams params = (PrintingRecipeParams) this.params;
            params.sound = sound;
        }

        @Override
        protected PrintingRecipeParams createParams() {
            return new PrintingRecipeParams();
        }

        @Override
        public Builder self() {
            return this;
        }
    }

    public static class Serializer<R extends PrintingRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<PrintingRecipeParams, R> factory) {
            this.codec = ProcessingRecipe.codec(factory, PrintingRecipeParams.CODEC);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, PrintingRecipeParams.STREAM_CODEC);
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
