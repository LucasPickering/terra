package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

public class OceanGenerator implements Generator {

    // Generation parameters
    // How far a tile has to be from the origin to be considered ocean
    private static final int OCEAN_DISTANCE_THRESHOLD = 14;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile tile : worldBuilder.getTiles().values()) {
            final int distance = tile.pos().distanceTo(TilePoint.ZERO);
            if (distance >= OCEAN_DISTANCE_THRESHOLD) {
                tile.setElevation((distance - OCEAN_DISTANCE_THRESHOLD + 1) * -10);
            }
        }
    }
}
