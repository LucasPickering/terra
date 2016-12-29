package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

public class BiomePainter implements Generator {

    // Generation parameters
    // Minimum elevation needed to be considered a mountain
    private static final int MIN_MOUNTAIN_ELEV = 40;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile tile : worldBuilder.getTiles().values()) {
            // If the biome has already been assigned, skip this tile
            if (tile.biome() != null) {
                continue;
            }
            final Biome biome;
            final int elevation = tile.elevation();
            if (elevation >= MIN_MOUNTAIN_ELEV) {
                biome = Biome.MOUNTAIN;
            } else {
                biome = Biome.FOREST;
            }
            tile.setBiome(biome);
        }
    }
}
