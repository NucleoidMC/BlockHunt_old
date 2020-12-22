/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

package me.lambdaurora.blockhunt;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.lambdaurora.blockhunt.game.BlockHuntPlayer;
import me.lambdaurora.blockhunt.game.BlockHuntWaiting;
import me.lambdaurora.blockhunt.game.config.BlockHuntConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the BlockHunt entrypoint.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlockHunt implements ModInitializer
{
    public static final String NAMESPACE = "blockhunt";
    private static BlockHunt INSTANCE;
    public final Logger logger = LogManager.getLogger(NAMESPACE);
    private final List<BlockHuntPlayer> activePlayers = new ArrayList<>();
    private final Int2ObjectMap<BlockHuntPlayer> activePlayersById = new Int2ObjectOpenHashMap<>();

    @Override
    public void onInitialize()
    {
        INSTANCE = this;

        GameType.register(new Identifier(NAMESPACE, "blockhunt"),
                BlockHuntWaiting::open,
                BlockHuntConfig.CODEC);
    }

    /**
     * Prints a message to the terminal.
     *
     * @param info the message to print
     */
    public void log(String info)
    {
        this.logger.info("[" + NAMESPACE + "] " + info);
    }

    public static @NotNull BlockHunt get()
    {
        return INSTANCE;
    }

    public static @NotNull Identifier mc(@NotNull String name)
    {
        return new Identifier(NAMESPACE, name);
    }

    public void addActivePlayer(@NotNull BlockHuntPlayer player)
    {
        this.activePlayers.add(player);
        this.activePlayersById.put(player.getPlayer().getEntityId(), player);
    }

    public void removeActivePlayer(@NotNull BlockHuntPlayer player)
    {
        this.activePlayers.remove(player);
        this.activePlayersById.remove(player.getPlayer().getEntityId());
    }

    public boolean isPlayerActive(@NotNull ServerPlayerEntity player)
    {
        return this.activePlayers.stream().anyMatch(p -> p.getPlayer() == player);
    }

    public Optional<BlockHuntPlayer> getActivePlayer(@NotNull ServerPlayerEntity player)
    {
        return this.activePlayers.stream().filter(p -> p.getPlayer() == player).findFirst();
    }

    public Optional<BlockHuntPlayer> getActivePlayer(int playerId)
    {
        return Optional.ofNullable(this.activePlayersById.get(playerId));
    }
}
