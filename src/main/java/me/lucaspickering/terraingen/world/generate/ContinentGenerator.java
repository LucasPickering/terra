package me.lucaspickering.terraingen.world.generate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Cluster;
import me.lucaspickering.terraingen.world.Tiles;
import me.lucaspickering.terraingen.world.tile.Tile;

public class ContinentGenerator implements Generator {

    // Range of number of continents to generate
    private static final IntRange CONTINENT_COUNT_RANGE = new IntRange(10, 20);

    // the range that a continent's target size can be in. Note that continents may end up being
    // smaller than the minimum of this range, if there aren't enough tiles to make them bigger.
    private static final IntRange CONTINENT_SIZE_RANGE = new IntRange(100, 1000);

    // Average size of each biome
    private static final int AVERAGE_BIOME_SIZE = 10;

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
    public void generate(Tiles tiles, Random random) {
        final Tiles nonContinentTiles = new Tiles(tiles); // Copy this because we'll be modifying it
        // Cluster tiles to make the continents
        final List<Cluster> continents = generateContinents(nonContinentTiles, random);

        // Adjust elevation to create oceans/coasts
        generateOceanFloor(nonContinentTiles, continents, random);

        // Paint biomes onto each continent
        continents.forEach(c -> paintContinent(c, random));
    }

    /**
     * Clusters together tiles to create a random number of continents. The given {@link Tiles}
     * instance will be modified so that all tiles that are put into a continent or adjacent to
     * a continent are removed. In other words, everything left in the Tiles object after this
     * function is called will be exactly the tiles that are not part of or adjacent to a continent.
     *
     * @param tiles  the tiles that make up the world
     * @param random the {@link Random} instance to use
     * @return the continents created
     */
    private List<Cluster> generateContinents(Tiles tiles, Random random) {
        final int numToGenerate = CONTINENT_COUNT_RANGE.randomIn(random);
        final List<Cluster> result = new ArrayList<>(numToGenerate);

        // While we haven't hit our target number and there are enough tiles left,
        // generate a new continent
        while (result.size() < numToGenerate && tiles.size() >= CONTINENT_SIZE_RANGE.min()) {
            result.add(generateContinent(tiles, random));
        }

        return result;
    }

    private Cluster generateContinent(Tiles availableTiles, Random random) {
        final Cluster cluster = new Cluster(); // The continent we are generating
        final int targetSize = CONTINENT_SIZE_RANGE.randomIn(random); // Pick a target size

        // Add the seed to the continent, and remove it from the pool of available tiles
        final Tile seed = Funcs.randomFromCollection(random, availableTiles); // The first tile
        cluster.add(seed);
        availableTiles.remove(seed); // No longer available

        // Keep adding until we hit our target size
        while (cluster.size() < targetSize) {
            final Tiles adjTiles = cluster.allAdjacents();
            adjTiles.retainAll(availableTiles);

            // Out of tiles to add
            if (adjTiles.isEmpty()) {
                break;
            }

            // Pick a random tile adjacent to the continent and add it in
            final Tile nextTile = Funcs.randomFromCollection(random, adjTiles);
            cluster.add(nextTile);
            availableTiles.remove(nextTile);
        }

        // Remove all tiles adjacent to this continent to ensure at least 1 tile of spacing
        // between all continents
        availableTiles.removeAll(cluster.allAdjacents());

        return cluster;
    }

    private void generateOceanFloor(Tiles nonContinentTiles, List<Cluster> continents,
                                    Random random) {
        nonContinentTiles.forEach(tile -> tile.setElevation(-20));

        // Make all tiles adjacent to each continent shallow, so they become coast
        for (Cluster continent : continents) {
            for (Tile tile : continent.allAdjacents()) {
                tile.setElevation(-6);
            }
        }
    }

    /**
     * "Paints" biomes onto the given continent.
     *
     * @param continent the continent to be painted
     * @param random    the {@link Random} instance to use
     */
    private void paintContinent(Cluster continent, Random random) {
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

        // Step 1
        final int numSeeds = continent.size() / AVERAGE_BIOME_SIZE;

        // Step 2
        final Tiles seeds = continent.selectTiles(random, numSeeds, 0);
        final Tiles unselectedTiles = new Tiles(continent); // We need a copy so we can modify it
        unselectedTiles.removeAll(seeds); // We've already selected the seeds, so remove them

        // Each biome, keyed by its seed
        final Map<TilePoint, Cluster> biomes = new HashMap<>();
        final Set<TilePoint> incompleteBiomes = new HashSet<>(); // Biomes with room to grow
        for (Tile seed : seeds) {
            // Pick a biome for this seed, then add it to the map
            final Cluster blotch = new Cluster();
            blotch.add(seed);
            biomes.put(seed.pos(), blotch);
            incompleteBiomes.add(seed.pos());
        }

        // Step 3 (the hard part)
        // While there are tiles left to assign...
        while (!unselectedTiles.isEmpty() && !incompleteBiomes.isEmpty()) {
            // Pick a seed that still has openings to work from
            final TilePoint seed = Funcs.randomFromCollection(random, incompleteBiomes);
            final Cluster biome = biomes.get(seed); // The biome grown from that seed

            final Tiles adjTiles = biome.allAdjacents(); // All tiles adjacent to this biome
            adjTiles.retainAll(unselectedTiles); // Remove tiles that are already in a biome

            if (adjTiles.isEmpty()) {
                // We've run out of ways to expand this biome, so consider it complete
                incompleteBiomes.remove(seed);
                continue;
            }

            // Pick one of those unassigned adjacent tiles, and add it to this biome
            final Tile tile = Funcs.randomFromCollection(random, adjTiles);
            biome.add(tile);
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

        for (Cluster blotch : biomes.values()) {
            final Biome biome = Funcs.randomFromCollection(random, allBiomes);
            blotch.forEach(tile -> tile.setBiome(biome)); // Set the biome for each tile
        }
    }
}
