package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.utils.GeneralFuncs;

/**
 * Randomly adjusts elevation of all land tiles to provide some small variation.
 */
public class LandRougher implements Generator {

    private static final int SLOP = 5; // Maximum amount a tile can be shifted in either direction
    private static final int WEIGHT = 0; // Tendency to shift up (positive) vs down (negative)

    @Override
    public void generate(World world, Random random) {
        for (Tile tile : world.getTiles()) {
            // If the tile is land, adjust its elevation a bit
            final int elev = GeneralFuncs.randomSlop(random, tile.elevation(), SLOP) + WEIGHT;
            tile.setElevation(elev);
        }
    }
}
