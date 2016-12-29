package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

public class OceanFloorGenerator implements Generator {

    // Generation parameters
    // How far a tile has to be from the origin to be considered coast
    private static final int COAST_DISTANCE_THRESHOLD = 14;
    private static final int COAST_DEPTH = -5; // Average depth of coast
    private static final int SHELF_DISTANCE_THRESHOLD = 15;
    private static final int SHELF_DEPTH = -13;
    private static final int FLOOR_DISTANCE_THRESHOLD = 17;
    private static final int FLOOR_DEPTH = -50;
    private static final int SLOP = 4;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile tile : worldBuilder.getTiles().values()) {
            final int distance = tile.pos().distanceTo(TilePoint.ZERO);
            final int depth;
            if (distance >= FLOOR_DISTANCE_THRESHOLD) {
                depth = FLOOR_DEPTH;
            } else if (distance >= SHELF_DISTANCE_THRESHOLD) {
                depth = SHELF_DEPTH;
            } else if (distance >= COAST_DISTANCE_THRESHOLD){
                depth = COAST_DEPTH;
            } else {
                continue; // This isn't going to become water, skip it
            }
            // Set the depth for this tile
            tile.setElevation(Funcs.randomSlop(random, depth, SLOP));
        }
    }
}
