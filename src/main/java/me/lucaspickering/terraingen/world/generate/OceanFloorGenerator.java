package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Generates low land that will eventually become coast/ocean.
 */
public class OceanFloorGenerator implements Generator {

    // Generation parameters
    // How far a tile has to be from the origin to be considered coast
    private static final int COAST_DISTANCE_THRESHOLD = 14;
    private static final int COAST_DEPTH = -5; // Average depth of coast
    private static final int SHELF_DISTANCE_THRESHOLD = 15;
    private static final int SHELF_DEPTH = -13;
    private static final int FLOOR_DISTANCE_THRESHOLD = 17;
    private static final int FLOOR_DEPTH = -50;

    @Override
    public void generate(Map<TilePoint, Tile> tiles, Random random) {
        for (Tile tile : tiles.values()) {
            final int distance = tile.pos().distanceTo(TilePoint.ZERO);
            final int elev;
            if (distance >= FLOOR_DISTANCE_THRESHOLD) {
                elev = FLOOR_DEPTH;
            } else if (distance >= SHELF_DISTANCE_THRESHOLD) {
                elev = SHELF_DEPTH;
            } else if (distance >= COAST_DISTANCE_THRESHOLD) {
                elev = COAST_DEPTH;
            } else {
                continue; // This isn't going to become water, skip it
            }
            tile.setElevation(elev); // Set the elev
        }
    }
}
