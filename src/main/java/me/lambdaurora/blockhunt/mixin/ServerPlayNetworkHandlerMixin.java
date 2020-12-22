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

package me.lambdaurora.blockhunt.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.lambdaurora.blockhunt.BlockHunt;
import me.lambdaurora.blockhunt.game.BlockHuntPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin
{
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener);

    @Inject(method = "teleportRequest", at = @At("RETURN"))
    private void onTeleportRequest(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> set, CallbackInfo ci)
    {
        BlockHunt.get().getActivePlayer(this.player).ifPresent(player -> player.updatePositionAndAngles(x, y, z, yaw, pitch));
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci)
    {
        if (packet instanceof EntityS2CPacketAccessor) {
            var positionPacket = (EntityS2CPacketAccessor) packet;
            BlockHunt.get().getActivePlayer(positionPacket.getId())
                    .map(BlockHuntPlayer::getHiderEntity)
                    .ifPresent(entity -> ci.cancel());
        } else if (packet instanceof EntityPositionS2CPacketAccessor) {
            var positionPacket = (EntityPositionS2CPacketAccessor) packet;
            BlockHunt.get().getActivePlayer(positionPacket.getId())
                    .map(BlockHuntPlayer::getHiderEntity)
                    .ifPresent(entity -> ci.cancel());
        } else if (packet instanceof EntityVelocityUpdateS2CPacketAccessor) {
            var velocityUpdatePacket = (EntityVelocityUpdateS2CPacketAccessor) packet;
            BlockHunt.get().getActivePlayer(velocityUpdatePacket.getId())
                    .map(BlockHuntPlayer::getHiderEntity)
                    .ifPresent(entity -> ci.cancel());
        }
    }
}
