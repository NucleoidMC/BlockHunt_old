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

package dev.lambdaurora.blockhunt.game.map;

import dev.lambdaurora.blockhunt.game.config.MapConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;

public class BlockHuntMap
{
    private final MapTemplate template;
    private final MapSpawn hiderSpawn;
    private final MapSpawn seekerSpawn;

    public BlockHuntMap(MapTemplate template, MapSpawn hiderSpawn, MapSpawn seekerSpawn)
    {
        this.template = template;
        this.hiderSpawn = hiderSpawn;
        this.seekerSpawn = seekerSpawn;
    }

    public MapTemplate getTemplate()
    {
        return this.template;
    }

    public MapSpawn getHiderSpawn()
    {
        return this.hiderSpawn;
    }

    public MapSpawn getSeekerSpawn()
    {
        return this.seekerSpawn;
    }

    public @NotNull ChunkGenerator asGenerator(@NotNull MinecraftServer server)
    {
        return new TemplateChunkGenerator(server, this.template);
    }

    public static BlockHuntMap of(MapConfig config) throws GameOpenException
    {
        MapTemplate template;
        try {
            template = MapTemplateSerializer.INSTANCE.loadFromResource(config.id);
        } catch (IOException e) {
            throw new GameOpenException(new TranslatableText("quakecraft.error.load_map", config.id.toString()), e);
        }

        var hiderSpawn = template.getMetadata().getFirstRegion("hider_spawn");
        if (hiderSpawn == null) {
            throw new GameOpenException(new LiteralText("No hider spawn defined."));
        }

        var seekerSpawn = template.getMetadata().getFirstRegion("seeker_spawn");
        if (seekerSpawn == null) {
            throw new GameOpenException(new LiteralText("No seeker spawn defined."));
        }

        var map = new BlockHuntMap(template, MapSpawn.of(hiderSpawn), MapSpawn.of(seekerSpawn));

        //template.setBiome(BuiltinBiomes.PLAINS);

        return map;
    }

    public static class MapSpawn
    {
        private final BlockBounds bounds;
        private final int angle;

        public MapSpawn(BlockBounds bounds, int angle)
        {
            this.bounds = bounds;
            this.angle = angle;
        }

        public BlockBounds getBounds()
        {
            return this.bounds;
        }

        public int getAngle()
        {
            return this.angle;
        }

        public void spawn(ServerWorld world, ServerPlayerEntity player)
        {
            var min = this.bounds.getMin();
            var max = this.bounds.getMax();

            double x = MathHelper.nextDouble(player.getRandom(), min.getX(), max.getX());
            double z = MathHelper.nextDouble(player.getRandom(), min.getZ(), max.getZ());
            double y = min.getY() + 0.5;

            player.teleport(world, x, y, z, this.angle, 0.f);
        }

        public static MapSpawn of(TemplateRegion region)
        {
            return new MapSpawn(region.getBounds(), region.getData().getInt("direction"));
        }
    }
}
