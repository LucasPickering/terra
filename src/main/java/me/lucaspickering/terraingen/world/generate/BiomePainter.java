package me.lucaspickering.terraingen.world.generate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.TilePoint;
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
        // Iterate over each blotch, and at each iteration, add one tile to that blotch that is
        // adjacent to it. Then, move onto the next blotch. Rinse and repeat until there are no
        // more tiles to assign.
        // Step 4 - assign the biomes
        // Iterate over each blotch and select the biome for that blotch, then assign the biome for
        // each tile in that blotch.

        // Step 1
        final int numSeeds = tiles.size() / AVERAGE_BLOTCH_SIZE;

        // Step 2
        final Tiles seeds = tiles.selectTiles(random, numSeeds, MIN_SEED_SPACING);

        // Step 3 (this one is a bit longer)
        final Tiles unselectedTiles = new Tiles(tiles); // We need a copy so we can modify it
        unselectedTiles.removeAll(seeds); // We already selected these

        // Each biome blotch, keyed by the seed of that blotch
        final Map<TilePoint, Tiles> blotches = new HashMap<>();
        final Set<TilePoint> incompleteBlotches = new HashSet<>(); // Blotches with room to grow
        for (Tile seed : seeds) {
            // Pick a biome for this seed, then add it to the map
            final Tiles blotch = new Tiles();
            blotch.add(seed);
            blotches.put(seed.pos(), blotch);
            incompleteBlotches.add(seed.pos());
        }

        // Step 3 (the hard part)
        // While there are tiles left to assign...
        while (!unselectedTiles.isEmpty()) {
            // Pick a seed that still has openings to work from
            final TilePoint seed = Funcs.randomFromCollection(random, incompleteBlotches);
            final Tiles blotch = blotches.get(seed); // The blotch grown from that seed

            // All tiles that are adjacent to any tile in this blotch
            final Tiles adjTiles = collectAdjacents(blotch);
            adjTiles.retainAll(unselectedTiles); // Remove tiles that are already in a blotch

            if (adjTiles.isEmpty()) {
                // We've run out of ways to expand this blotch, so consider it complete
                incompleteBlotches.remove(seed);
                continue;
            }

            // Pick one of those unassigned adjacent tiles, and add it to this blotch
            final Tile tile = Funcs.randomFromCollection(random, adjTiles);
            blotch.add(tile);
            unselectedTiles.remove(tile);
        }

        // Step 4 - cleanup
        for (Tiles blotch : blotches.values()) {
            final Biome biome = Funcs.randomFromCollection(random, Biome.REGULAR_LAND_BIOMES);
            blotch.forEach(tile -> tile.setBiome(biome)); // Set the biome for each tile
        }
    }

    private Tiles collectAdjacents(Tiles tiles) {
        final Tiles result = new Tiles();
        for (Tile tile : tiles) {
            result.addAll(tile.adjacents().values());
        }
        return result;
    }
}
