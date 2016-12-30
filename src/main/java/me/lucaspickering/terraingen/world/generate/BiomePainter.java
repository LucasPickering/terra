package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Paints biomes onto each continent.
 */
public class BiomePainter implements Generator {

    @Override
    public void generate(Map<TilePoint, Tile> tiles, Random random) {
        for (Tile tile : tiles.values()) {
            // If the biome has already been assigned, skip this tile
            if (tile.biome() != null) {
                continue;
            }
            final Biome biome = Biome.FOREST;
            tile.setBiome(biome);
        }
    }
}
