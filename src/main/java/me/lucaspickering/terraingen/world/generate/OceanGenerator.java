package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

public class OceanGenerator implements Generator {

    // Generation parameters
    // How far a tile has to be from the origin to be considered ocean
    private static final int OCEAN_DISTANCE_THRESHOLD = 7;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile.Builder builder : worldBuilder.builders().values()) {
            final int distance = builder.getPos().distanceTo(TilePoint.ZERO);
            if (distance >= OCEAN_DISTANCE_THRESHOLD) {
                builder.setBiome(Biome.OCEAN);
                builder.setElevation(-distance + OCEAN_DISTANCE_THRESHOLD - 1);
            }
        }
    }
}
