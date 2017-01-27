package me.lucaspickering.terraingen.world.generate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.Tile;

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

    // The biomes that we can paint in this routine, and the relative chance that each one will
    // be selected
    public static final Map<Biome, Integer> BIOME_WEIGHTS = new EnumMap<>(Biome.class);

    // Initialize all the weights
    static {
        BIOME_WEIGHTS.put(Biome.PLAINS, 10);
        BIOME_WEIGHTS.put(Biome.FOREST, 10);
        BIOME_WEIGHTS.put(Biome.DESERT, 2);
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

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
        final int numSeeds = worldTiles.size() / AVERAGE_BLOTCH_SIZE;

        // Step 2
        final TileSet seeds = worldTiles.selectTiles(random, numSeeds, MIN_SEED_SPACING);
        final TileSet unselectedTiles = new TileSet(worldTiles); // We need a copy so we can modify it
        unselectedTiles.removeAll(seeds); // We've already selected the seeds, so remove them

        // Each biome blotch, keyed by its seed
        final Map<TilePoint, Cluster> blotches = new HashMap<>();

        // All the blotches that still have room to grow
        final Map<TilePoint, Cluster> incompleteBlotches = new HashMap<>();

        for (Tile seed : seeds) {
            // Add each seed to its cluster, and each cluster to the maps
            final Cluster blotch = Cluster.fromWorld(worldTiles);
            blotch.add(seed);
            blotches.put(seed.pos(), blotch);
            incompleteBlotches.put(seed.pos(), blotch);
        }

        // Step 3 (the hard part)
        // While there are tiles left to assign...
        while (!unselectedTiles.isEmpty()) {
            if (incompleteBlotches.isEmpty()) {
                throw new IllegalStateException(
                    "No biomes to grow, but there are tiles left to assign");
            }

            // Get the smallest biome and try to grow it
            Map.Entry<TilePoint, Cluster> smallestBlotch = null;
            for (Map.Entry<TilePoint, Cluster> entry : incompleteBlotches.entrySet()) {
                if (smallestBlotch == null || entry.getValue().size()
                                              < smallestBlotch.getValue().size()) {
                    smallestBlotch = entry;
                }
            }
            assert smallestBlotch != null; // Should have been assigned
            final TilePoint seed = smallestBlotch.getKey();
            final Cluster blotch = smallestBlotch.getValue();

            final TileSet adjTiles = blotch.allAdjacents(); // All tiles adjacent to this blotch
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

        // Step 4
        // Create a list of all the selected biomes, with more entries for biomes that are more
        // likely to be picked, so that one can be picked at random with proper chances.
        final int totalWeight = BIOME_WEIGHTS.values().stream().reduce(0, (acc, i) -> acc + i);
        final List<Biome> allBiomes = new ArrayList<>(totalWeight);
        for (Map.Entry<Biome, Integer> entry : BIOME_WEIGHTS.entrySet()) {
            final Biome biome = entry.getKey();
            final int weight = entry.getValue();
            // Put weight entries in the list for this biome
            for (int i = 0; i < weight; i++) {
                allBiomes.add(biome);
            }
        }

        for (Cluster blotch : blotches.values()) {
            final Biome biome = Funcs.randomFromCollection(random, allBiomes);
            blotch.forEach(tile -> tile.setBiome(biome)); // Set the biome for each tile
        }
    }
}
