package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.HexPoint;

/**
 * Represents one stage in the world generation process. A {@link Generator} is given a
 * {@link Map} of {@link HexPoint}s to {@link Tile}s, representing a partially-generated
 * world. It modifies the tiles in that map, and returns the same map with all the same objects.
 *
 * A specific instance of this type should only be used once. If you want to generate a new
 * world, create a new instance of each generator you want to use to ensure that no state is left
 * over from the previous generation.
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
