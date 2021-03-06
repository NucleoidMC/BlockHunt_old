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

package dev.lambdaurora.blockhunt.game;

import com.google.common.collect.Multimap;
import dev.lambdaurora.blockhunt.entity.HiderBlockEntity;
import dev.lambdaurora.blockhunt.game.config.BlockHuntConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import dev.lambdaurora.blockhunt.game.map.BlockHuntMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import java.util.UUID;

public class BlockHuntGame
{
    private final GameSpace space;
    private final BlockHuntConfig config;
    private final BlockHuntMap map;
    private final Object2ObjectMap<UUID, BlockHuntPlayer> players = new Object2ObjectOpenHashMap<>();
    private boolean running = false;
    private boolean end = false;
    private int time;
    private int endTime = 10 * 20;

    public BlockHuntGame(GameLogic logic, BlockHuntConfig config, BlockHuntMap map)
    {
        this.space = logic.getSpace();
        this.config = config;
        this.map = map;
        this.time = this.config.time;
    }

    /**
     * Opens the game.
     *
     * @param logic the game logic
     * @param config the game configuration
     * @param map the game map
     * @param players the players affected to teams
     */
    public static void open(@NotNull GameLogic logic, @NotNull BlockHuntConfig config, @NotNull BlockHuntMap map,
                            @NotNull Multimap<GameTeam, ServerPlayerEntity> players)
    {
        logic.getSpace().openGame(game -> {
            BlockHuntGame active = new BlockHuntGame(logic, config, map);
            players.forEach((team, player) ->
                    active.players.put(player.getUuid(), new BlockHuntPlayer(active, player, team))
            );

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);

            game.getSpace().getWorld().getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, null);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            //game.on(PlayerDamageListener.EVENT, active::onDamage);
            //game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);

            //game.on(UseBlockListener.EVENT, active::onUseBlock);
            //game.on(UseItemListener.EVENT, active::onUseItem);
            //game.on(HandSwingListener.EVENT, active::onSwingHand);
            game.on(AttackEntityListener.EVENT, active::onAttackEntity);
        });
    }

    protected void onOpen()
    {
        this.players.forEach((uuid, player) -> {


            player.spawn();
        });
        this.running = true;
        //this.scoreboard.update();
    }

    protected void onClose()
    {
    }

    public void tick()
    {
        if (this.running) {
            int[] activePlayer = new int[]{0};
            this.players.forEach((uuid, player) -> {
                if (player.hasLeft())
                    return;

                //player.tick(this.space);
                activePlayer[0]++;
            });
            this.time--;

            if (activePlayer[0] <= 0) {
                this.space.getPlayers().sendMessage(new TranslatableText("blockhunt.game.end.not_enough_players").formatted(Formatting.RED));
                this.space.close();
            }

            if (this.time <= 0) {
                this.space.getPlayers().sendMessage(new TranslatableText("blockhunt.game.end.hider_won").formatted(Formatting.GREEN));
                this.end = true;
            }

            if (this.end) {
                //this.participants.forEach((uuid, participant) -> participant.onEnd());
            }
        } else if (this.end) {
            this.endTime--;

            /*if (this.endTime % 20 == 0) {
                this.winners.forEach(player -> {
                    if (!player.hasLeft()) {
                        ServerPlayerEntity mcPlayer = player.getPlayer();
                        if (mcPlayer == null)
                            return;

                        Quakecraft.spawnFirework(this.space.getWorld(), mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ(), new int[]{15435844, 11743532}, false, -1);
                    }
                });
            }*/

            if (this.endTime == 0)
                this.space.close();
        }

        //this.scoreboard.update();
    }

    private void addPlayer(@NotNull ServerPlayerEntity player)
    {

    }

    private void removePlayer(@NotNull ServerPlayerEntity player)
    {
        var participant = this.players.get(player.getUuid());
        if (participant != null)
            participant.onLeave();
    }

    private @NotNull ActionResult onAttackEntity(ServerPlayerEntity player, Hand hand, Entity entity, EntityHitResult entityHitResult)
    {
        if (player.interactionManager.getGameMode() == GameMode.SPECTATOR)
            return ActionResult.PASS;
        return entity instanceof HiderBlockEntity ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    public BlockHuntMap getMap()
    {
        return this.map;
    }

    public GameSpace getSpace()
    {
        return this.space;
    }

    public ServerWorld getWorld()
    {
        return this.space.getWorld();
    }
}
