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

package me.lambdaurora.blockhunt.game;

import me.lambdaurora.blockhunt.BlockHunt;
import me.lambdaurora.blockhunt.BlockHuntConstants;
import me.lambdaurora.blockhunt.entity.HiderBlockEntity;
import me.lambdaurora.blockhunt.game.map.BlockHuntMap;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.player.GameTeam;

/**
 * Represents a BlockHunt player.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlockHuntPlayer
{
    private final BlockHuntGame game;
    private ServerPlayerEntity player;
    private GameTeam team;
    private HiderBlockEntity hiderEntity;
    private boolean dead = false;

    public BlockHuntPlayer(BlockHuntGame game, ServerPlayerEntity player, GameTeam team)
    {
        this.game = game;
        this.player = player;
        this.team = team;
        BlockHunt.get().addActivePlayer(this);
    }

    public ServerPlayerEntity getPlayer()
    {
        return this.player;
    }

    public GameTeam getTeam()
    {
        return this.team;
    }

    public HiderBlockEntity getHiderEntity()
    {
        return this.hiderEntity;
    }

    /**
     * Spawns the player.
     */
    public void spawn()
    {
        BlockHuntMap.MapSpawn spawn = this.game.getMap().getHiderSpawn();
        if (this.team == BlockHuntConstants.SEEKER_TEAM)
            spawn = this.game.getMap().getSeekerSpawn();
        spawn.spawn(this.game.getWorld(), this.player);

        hiderEntity = new HiderBlockEntity(this, Blocks.GLASS.getDefaultState());
        hiderEntity.setNoGravity(true);
        //hiderEntity.startRiding(this.player, true);
        this.game.getWorld().spawnEntity(hiderEntity);
    }

    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        if (hiderEntity != null) {
            //this.player.sendMessage(new LiteralText("OH NO " + x + " ; " + z), false);
            hiderEntity.updatePositionAndAngles(x, y, z, yaw, pitch);
            var packet = new EntityPositionS2CPacket(hiderEntity);
            this.game.getSpace().getPlayers().sendPacket(packet);
        }
    }

    public void onLeave()
    {
        if (hiderEntity != null) {
            hiderEntity.remove();
            hiderEntity = null;
        }
        BlockHunt.get().removeActivePlayer(this);
        this.player = null;
    }

    public boolean hasLeft()
    {
        return this.player == null;
    }
}
