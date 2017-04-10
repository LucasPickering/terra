package me.lucaspickering.terraingen.world.generate.unused;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileMap;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.GeneralFuncs;

/**
 * Paints biomes onto each continent. Without adjusting elevation, each tile is assigned a land
 * biome. The biomes are assigned in blotches. These tiles can be changed to other biomes (e.g.
 * ocean, lake) later on, and can have the elevation adjusted. This is a very early step in the
 * generation process.
 */
public class ClusterBiomePainter implements Generator {

    // Average size of each biome blotch
    private static final int AVERAGE_BLOTCH_SIZE = 10;
    private static final int MIN_MOUNTAIN_ELEV = 30;

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
        for (Continent continent : world.getContinents()) {
            paintContinent(continent, random);
        }
    }

    /**
     * "Paints" biomes onto the given continent.
     *
     * @param continent the continent to be painted
     * @param random    the {@link Random} instance to use
     */
    private void paintContinent(Continent continent, Random random) {
        // Step 1 - calculate n
        // Figure out how many biome biomes we want
        // n = number of tiles / average size of blotch
        // Step 2 - select seeds
        // Pick n tiles to be "seed tiles", i.e. the first tiles of their respective biomes.
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

        final Cluster continentCluster = continent.getTiles();

        // Step 1
        final int numSeeds = continentCluster.size() / AVERAGE_BLOTCH_SIZE;

        // Step 2
        final TileSet seeds = continentCluster.selectTiles(random, numSeeds, 0);
        final TileSet unselectedTiles = new TileSet(continentCluster); // Make a copy
        unselectedTiles.removeAll(seeds); // We've already selected the seeds, so remove them

        // Each biome, keyed by its seed
        final TileMap<Cluster> biomes = new TileMap<>();
        final TileSet incompleteBiomes = new TileSet(); // Biomes with room to grow
        for (Tile seed : seeds) {
            // Use the continent as the "world" for this biome blotch
            final Cluster blotch = new Cluster(continentCluster);
            // Add the seed to the biome
            blotch.add(seed);
            biomes.put(seed, blotch);
            incompleteBiomes.add(seed);
        }

        // Step 3 (the hard part)
        // While there are tiles left to assign...
        while (!unselectedTiles.isEmpty() && !incompleteBiomes.isEmpty()) {
            // Pick a seed that still has openings to work from
            final Tile seed = GeneralFuncs.randomFromCollection(random, incompleteBiomes);
            final Cluster biome = biomes.get(seed); // The biome grown from that seed

            final TileSet adjTiles = biome.allAdjacents(); // All tiles adjacent to this biome
            adjTiles.retainAll(unselectedTiles); // Remove tiles that are already in a biome

            if (adjTiles.isEmpty()) {
                // We've run out of ways to expand this biome, so consider it complete
                incompleteBiomes.remove(seed);
                continue;
            }

            // Pick one of those unassigned adjacent tiles, and add it to this biome
            final Tile tile = GeneralFuncs.randomFromCollection(random, adjTiles);
            biome.add(tile);
            unselectedTiles.remove(tile);
        }

        // Step 4
        // Pick a biome for each cluster, using weighted chance as defined in BIOME_WEIGHTS
        for (Cluster blotch : biomes.values()) {
            final Biome biome = GeneralFuncs.randomFromCollectionWeighted(random,
                                                                          BIOME_WEIGHTS.keySet(),
                                                                          BIOME_WEIGHTS::get);

            // Set the biome for each tile in the cluster, if it doesnt have a biome already
            for (Tile tile : blotch) {
                if (tile.biome() == Biome.NONE) {
                    // If the tile is at high elevation, make it mountain, otherwise normal biome
                    if (tile.elevation() >= MIN_MOUNTAIN_ELEV) {
                        tile.setBiome(Biome.MOUNTAIN);
                    } else {
                        tile.setBiome(biome);
                    }
                }
            }
        }
    }
}
