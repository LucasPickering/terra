package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.WorldContainer;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Randomly adjusts elevation of all land tiles to provide some small variation.
 */
public class LandRougher implements Generator {

    private static final int SLOP = 3; // Maximum amount a tile can be shifted in either direction
    private static final int WEIGHT = 2; // Tendency to shift up (positive) vs down (negative)

    @Override
    public void generate(WorldContainer world, Random random) {
        for (Tile tile : world.getTiles()) {
            // If the tile is land, adjust its elevation a bit
            final int elev = Funcs.randomSlop(random, tile.elevation(), SLOP) + WEIGHT;
            tile.setElevation(elev);
        }
    }
}
