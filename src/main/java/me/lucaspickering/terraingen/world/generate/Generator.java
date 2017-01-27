package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TilePoint;

/**
 * Represents one stage in the world generation process. A {@link Generator} is given a
 * {@link Map} of {@link TilePoint}s to {@link Tile}s, representing a partially-generated
 * world. It modifies the tiles in that map, and returns the same map with all the same objects.
 *
 * Given the same {@link World} and {@link Random}, a {@link Generator} must make the exact same
 * modification to the world in any two calls of its {@link #generate(World, Random)} function.
 * In other words, it must be stateless.
 */
@FunctionalInterface
public interface Generator {

    /**
     * Modifies the given world, generating new terrain features.
     *
     * @param world  the world to modify
     * @param random the source of random for generation
     */
    void generate(World world, Random random);

}
