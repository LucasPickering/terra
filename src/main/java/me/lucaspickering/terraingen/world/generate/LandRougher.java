package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Randomly adjusts elevation of all land tiles to provide some small variation.
 */
public class LandRougher implements Generator {

    private static final int SLOP = 3;
    private static final int WEIGHT = 2;

    @Override
    public void generate(Map<TilePoint, Tile> tiles, Random random) {
        for (Tile tile : tiles.values()) {
            // If the tile is land, adjust its elevation a bit
            final int elev = Funcs.randomSlop(random, tile.elevation(), SLOP) + WEIGHT;
            tile.setElevation(elev);
        }
    }
}
