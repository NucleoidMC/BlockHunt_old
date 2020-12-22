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

import me.lambdaurora.blockhunt.BlockHunt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity
{
    public ServerPlayerEntityMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

/*    @Override
    public void setVelocity(Vec3d velocity)
    {
        super.setVelocity(velocity);
        BlockHunt.get().getActivePlayer((ServerPlayerEntity) (Object) this)
                .ifPresent(player -> {
                    var entity = player.getHiderEntity();
                    if (entity != null)
                        entity.setVelocity(velocity);
                });
    }*/


    @Override
    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch)
    {
        BlockHunt.get().getActivePlayer((ServerPlayerEntity) (Object) this)
                .ifPresent(player -> player.updatePositionAndAngles(x, y, z, yaw, pitch));
        super.updatePositionAndAngles(x, y, z, yaw, pitch);
    }

    /*@Inject(method = "tick", at = @At("RETURN"))
    private void onMove(CallbackInfo ci)
    {
        BlockHunt.get().getActivePlayer((ServerPlayerEntity) (Object) this)
                .ifPresent(player -> player.onTeleportRequest(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch));
    }*/
}
