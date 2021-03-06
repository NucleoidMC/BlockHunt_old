/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.blockhunt.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class BlockHuntConfig
{
    public static final Codec<BlockHuntConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MapConfig.CODEC.fieldOf("map").forGetter(config -> config.map),
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
            Codec.INT.optionalFieldOf("time", 20 * 60 * 20).forGetter(config -> config.time)
    ).apply(instance, BlockHuntConfig::new));

    public final MapConfig map;
    public final PlayerConfig players;
    public final int time;

    public BlockHuntConfig(@NotNull MapConfig map,
                           @NotNull PlayerConfig players,
                           int time)
    {
        this.map = map;
        this.players = players;
        this.time = time;
    }
}
