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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import plus.dragons.createenchantmentindustry.platform.create.ProcessingRecipeParams;

public class PrintingRecipeParams extends ProcessingRecipeParams {
    protected static final Codec<PlaySoundEffect> SOUND_CODEC = Codec.either(
            BuiltInRegistries.SOUND_EVENT.holderByNameCodec(),
            PlaySoundEffect.CODEC.codec()).xmap(
                    either -> either.map(
                            sound -> new PlaySoundEffect(List.of(sound), ConstantFloat.of(1f), ConstantFloat.of(1f)),
                            Function.identity()),
                    Either::right);
    protected static final StreamCodec<ByteBuf, PlaySoundEffect> SOUND_STREAM_CODEC = ByteBufCodecs.fromCodec(SOUND_CODEC);
    public static final MapCodec<PrintingRecipeParams> CODEC = RecordCodecBuilder.<PrintingRecipeParams>mapCodec(
            instance -> instance.group(
                    codec(PrintingRecipeParams::new).forGetter(Function.identity()),
                    SOUND_CODEC.fieldOf("sound").forGetter(PrintingRecipeParams::getSound)).apply(instance, PrintingRecipeParams::setSound))
            .validate(PrintingRecipeParams::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, PrintingRecipeParams> STREAM_CODEC = streamCodec(PrintingRecipeParams::new);
    protected PlaySoundEffect sound;

    protected PrintingRecipeParams() {
        super();
    }

    public PrintingRecipeParams(PlaySoundEffect sound) {
        super();
        this.sound = sound;
    }

    protected PlaySoundEffect getSound() {
        return sound;
    }

    protected PrintingRecipeParams setSound(PlaySoundEffect sound) {
        this.sound = sound;
        return this;
    }

    protected DataResult<PrintingRecipeParams> validate() {
        if (ingredients.size() != 2)
            return DataResult.error(() -> "Printing recipe must have 2 item inputs, [0]: input, [1]: template");
        if (fluidIngredients.size() != 1)
            return DataResult.error(() -> "Printing recipe must have 1 fluid input");
        return DataResult.success(this);
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        SOUND_STREAM_CODEC.encode(buffer, sound);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        sound = SOUND_STREAM_CODEC.decode(buffer);
    }
}
