package me.lucaspickering.groundwar.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.WorldBuilder;
import me.lucaspickering.groundwar.world.tile.Tile;

/**
 * Represents one stage in the world generation process. A {@link Generator} is given a
 * {@link Map} of {@link TilePoint}s to {@link Tile.Builder}s, representing a partially-generated
 * world. It modifies the tiles in that map, and returns the same map with all the same objects.
 */
@FunctionalInterface
public interface Generator {

    /**
     * Modifies the given world, generating new terrain features.
     *
     * @param worldBuilder the world to modify
     * @param random       the source of random for generation
     */
    void generate(WorldBuilder worldBuilder, Random random);

}
