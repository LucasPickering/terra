package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Tiles;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Paints biomes onto each continent. Without adjusting elevation, each tile is assigned a land
 * biome. The biomes are assigned in blotches. These tiles can be changed to other biomes (e.g.
 * ocean, lake) later on, and can have the elevation adjusted. This is a very early step in the
 * generation process.
 */
public class BiomePainter implements Generator {

    // Average size of each biome blotch
    private static final int AVERAGE_BLOTCH_SIZE = 10;

    // Minimum number of tiles separating each biome seed
    private static final int MIN_SEED_SPACING = 2;

    @Override
    public void generate(Tiles tiles, Random random) {
        // Step 1 - calculate n
        // Figure out how many biome blotches we want
        // n = number of tiles / average size of blotch
        // Step 2 - select seeds
        // Pick n tiles to be "seed tiles", i.e. the first tiles of their respective blotches.
        // The seeds have a minimum spacing from each other, which is enforced now.
        // Step 3 - grow seeds
        // Each blotch will be grown from its seed to be about average size.
        // By the end of this step, every tile will have been assigned.

        final int numSeeds = tiles.size() / AVERAGE_BLOTCH_SIZE;
        final Tiles seeds = WorldHelper.selectTiles(tiles, random, numSeeds, MIN_SEED_SPACING);

        for (Tile seed : seeds) {
            seed.setBiome(Funcs.randomFromCollection(random, Biome.REGULAR_LAND_BIOMES));
        }

        for (Tile tile : tiles) {
            if (tile.biome() != null) {
                continue;
            }
            final Biome biome = Biome.FOREST;
            tile.setBiome(biome);
        }
    }
}
