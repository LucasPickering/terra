package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Paints biomes onto each continent.
 */
public class BiomePainter implements Generator {

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        for (Tile tile : worldBuilder.getTiles().values()) {
            // If the biome has already been assigned, skip this tile
            if (tile.biome() != null) {
                continue;
            }
            final Biome biome = Biome.PLAINS;
            tile.setBiome(biome);
        }
    }
}
