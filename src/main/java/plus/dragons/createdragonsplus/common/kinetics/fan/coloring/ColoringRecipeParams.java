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

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import plus.dragons.createdragonsplus.platform.create.ProcessingRecipeParams;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;

@FieldsNullabilityUnknownByDefault
public class ColoringRecipeParams extends ProcessingRecipeParams {
    public static final MapCodec<ColoringRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            codec(ColoringRecipeParams::new).forGetter(Function.identity()),
            Identifier.CODEC.fieldOf("color").forGetter(ColoringRecipeParams::getColor)).apply(instance, ColoringRecipeParams::setColor));
    public static final StreamCodec<RegistryFriendlyByteBuf, ColoringRecipeParams> STREAM_CODEC = streamCodec(ColoringRecipeParams::new);
    protected Identifier color;

    protected ColoringRecipeParams() {
        super();
    }

    public ColoringRecipeParams(Identifier color) {
        this.color = color;
    }

    protected Identifier getColor() {
        return color;
    }

    protected ColoringRecipeParams setColor(Identifier color) {
        this.color = color;
        return this;
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        Identifier.STREAM_CODEC.encode(buffer, color);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        color = Identifier.STREAM_CODEC.decode(buffer);
    }
}
