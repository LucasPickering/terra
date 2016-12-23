package me.lucaspickering.groundwar.world.generate;

import java.util.Random;

import me.lucaspickering.groundwar.world.Biome;
import me.lucaspickering.groundwar.world.WorldBuilder;
import me.lucaspickering.groundwar.world.tile.Tile;

public class BiomeGenerator implements Generator {

    // Generation parameters
    // Minimum elevation needed to be considered a mountain
    private static final int MIN_MOUNTAIN_ELEV = 40;

    // Minimum elevation needed to be considered foothills
    private static final int MIN_FOOTHILLS_ELEV = 25;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile.Builder builder : worldBuilder.builders().values()) {
            final Biome biome;
            if (builder.getElevation() >= MIN_MOUNTAIN_ELEV) {
                biome = Biome.PLAINS;
            } else if (builder.getElevation() >= MIN_FOOTHILLS_ELEV) {
                biome = Biome.PLAINS;
            } else {
                biome = Biome.PLAINS;
            }
            builder.setBiome(biome);
        }
    }
}
