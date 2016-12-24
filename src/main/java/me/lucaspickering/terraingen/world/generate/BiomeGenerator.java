package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

public class BiomeGenerator implements Generator {

    // Generation parameters
    // Minimum elevation needed to be considered a mountain
    private static final int MIN_MOUNTAIN_ELEV = 40;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile.Builder builder : worldBuilder.builders().values()) {
            // If the biome has already been assigned, skip this tile
            if (builder.getBiome() != null) {
                continue;
            }
            final Biome biome;
            final int elevation = builder.getElevation();
            if (elevation >= MIN_MOUNTAIN_ELEV) {
                biome = Biome.PLAINS;
            } else {
                biome = Biome.PLAINS;
            }
            builder.setBiome(biome);
        }
    }
}
