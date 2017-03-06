package me.lucaspickering.terraingen.world.generate.unused;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileMap;
import me.lucaspickering.terraingen.world.util.TileSet;

/**
 * Generates continents by clustering tiles together.
 */
public class ContinentGenerator implements Generator {

    // Range of number of continents to generate
    private static final IntRange CONTINENT_COUNT_RANGE = new IntRange(10, 20);

    // the range that a continent's target size can be in. Note that continents may end up being
    // smaller than the minimum of this range, if there aren't enough tiles to make them bigger.
    private static final IntRange CONTINENT_SIZE_RANGE = new IntRange(100, 1000);

    private World world; // The world being operated on
    private Random random;
    private TileSet unassignedTiles; // All tiles that aren't currently assigned to a continent

    @Override
    public void generate(World world, Random random) {
        this.world = world;
        this.random = random;
        this.unassignedTiles = new TileSet(world.getTiles()); // Make a copy so we can modify it

        // Cluster tiles into continents
        generateContinents();

        // Adjust elevation to create oceans/coasts
        generateOceanFloor();
    }

    /**
     * Clusters together tiles to create a random number of continents. The generated continents
     * will be added to {@link #world}.
     */
    private void generateContinents() {
        final int numToGenerate = CONTINENT_COUNT_RANGE.randomIn(random);

        // While we haven't hit our target number and there are enough tiles left,
        // generate a new continent
        while (world.getContinents().size() < numToGenerate
               && unassignedTiles.size() >= CONTINENT_SIZE_RANGE.min()) {
            final Continent continent = generateContinent();
            // If the continent is null, that means that it was generated, but merged into
            // another continent that is already in the list.
            if (continent != null) {
                world.getContinents().add(continent);
            }
        }

        fillContinentHoles(); // Fill holes in the continents
        reclusterContinents(); // Join continents that grew to meet each other
        smoothContinents(); // Smooth the coast of each continent
    }

    /**
     * Generates a single continent from the given collection of available tiles.
     *
     * @return the generated continent
     */
    private Continent generateContinent() {
        final Cluster cluster = new Cluster(world.getTiles()); // The continent
        final Continent continent = new Continent(cluster); // Put the cluster into a continent
        final int targetSize = CONTINENT_SIZE_RANGE.randomIn(random); // Pick a target size

        // Pick a random seed, add it to the continent, and remove it from the pool
        final Tile seed = Funcs.randomFromCollection(random, unassignedTiles);
        addToContinent(seed, continent);

        // Keep adding until we hit our target size
        while (cluster.size() < targetSize) {
            // If a tile is adjacent to any tile in the continent, it becomes a candidate
            final TileSet candidates = cluster.allAdjacents();
            candidates.retainAll(unassignedTiles); // Filter out tiles that aren't available

            // No candidates, done with this continent
            if (candidates.isEmpty()) {
                break;
            }

            // Pick a random tile adjacent to the continent and add it in
            final Tile nextTile = Funcs.randomFromCollection(random, candidates);
            addToContinent(nextTile, continent);
        }

        assert !cluster.isEmpty(); // At least one tile should have been added

        return continent;
    }

    private void reclusterContinents() {
        final List<Continent> continents = world.getContinents();
        continents.clear();
        final TileMap<Continent> tilesToContinents = world.getTilesToContinents();

        // All tiles that are in a continent
        final TileSet allTiles = new TileSet(tilesToContinents.keySet());

        // Recluster all the continent tiles
        final List<Cluster> newClusters = allTiles.cluster();
        for (Cluster cluster : newClusters) {
            // Copy each cluster into the entire world so that their adjacent tiles get tracked
            final Cluster copiedCluster = new Cluster(cluster, world.getTiles());
            final Continent continent = new Continent(copiedCluster);
            continents.add(continent);
            for (Tile tile : cluster) {
                tilesToContinents.put(tile, new Continent(cluster));
            }
        }
    }

    /**
     * Fills holes in each continent. A hole is one or more tiles inside a continent that have
     * not been assigned to that continent. The hole is too small to become an ocean, so if it
     * doesn't get added to the continent, it will probably just be a black spot on the map.
     */
    private void fillContinentHoles() {
        // Cluster the negative tiles
        final List<Cluster> nonContinentClusters = unassignedTiles.cluster();

        // Fill in the "holes" in each continent, i.e. find all clusters that are entirely inside
        // one continent, and add them into that continent.
        for (Cluster nonContinentCluster : nonContinentClusters) {
            // If the cluster is small enough that it won't become an ocean, then it must be
            // completely surrounded by one continent. Add it to that continent.
            if (nonContinentCluster.size() < WaterPainter.MIN_OCEAN_SIZE) {
                // Copy the cluster so that it exists in the context of the entire world, then
                // get the continent that borders it.
                final Cluster copiedCluster = new Cluster(nonContinentCluster, world.getTiles());

                // Pick any tile adjacent to this cluster, and figure out what continent it's in
                final Tile adjTile = Funcs.firstFromCollection(copiedCluster.allAdjacents());
                final Continent surroundingContinent = world.getTilesToContinents().get(adjTile);

                // Add the cluster to the adjacent continent
                for (Tile tile : nonContinentCluster) {
                    addToContinent(tile, surroundingContinent);
                }
            }
        }
    }

    private void smoothContinents() {
        // Smooth each continent
        world.getContinents().forEach(this::smoothContinent);
    }

    /**
     * Smooth the coast of continents by removing thin bits of land that stick out.
     *
     * @param continent the continent to be smoothed
     */
    private void smoothContinent(Continent continent) {
        final Cluster cluster = continent.getTiles();

        // Keep two queues: one of tiles that need to be checked for removal and one of tiles that
        // need to be removed.
        final List<Tile> toCheck = new LinkedList<>(cluster);
        final List<Tile> toRemove = new LinkedList<>();

        // 1. Check all queued tiles for removal
        // 2. Remove tiles that were marked as such
        // 3. Any tile adjacent to a removed tile gets queued to be checked again
        // 4. Repeat until there are no more tiles to check
        while (!toCheck.isEmpty()) {
            // Check all queued tiles
            for (Tile tile : toCheck) {
                // Check if this tile should be removed
                if (smoothingShouldRemove(tile, continent)) {
                    toRemove.add(tile); // Queue it for removal
                }
            }
            toCheck.clear(); // We checked them all, clear the queue

            // Remove all queued tiles
            for (Tile tile : toRemove) {
                // We'll need to check all tiles adjacent to this one for removal later
                final Collection<Tile> adjTiles = cluster.getAdjacentTiles(tile).values();
                toCheck.addAll(adjTiles);

                removeFromContinent(tile, continent); // Remove the tile
            }
            toRemove.clear(); // We removed them all, clear the queue
        }
    }

    private boolean smoothingShouldRemove(Tile tile, Continent continent) {
        final Map<Direction, Tile> adjTiles = continent.getTiles().getAdjacentTiles(tile);

        // If the tile borders only 1 other tile in the continent, it should be removed
        if (adjTiles.size() <= 1) {
            return true;
        }

        // Check if it borders only two tiles that aren't adjacent to each other (i.e. check if
        // this tile is a "bridge")
        if (adjTiles.size() == 2) {
            final List<Direction> dir = new ArrayList<>(adjTiles.keySet());
            // Check that the two directions aren't adjacent to each other
            if (!dir.get(0).isAdjacentTo(dir.get(1))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds the given tile to the given continent and also removes the tile from the collection of
     * unassigned tiles. Assuming the tile is added to the continent, then
     * {@link World#getTilesToContinents()} will be updated.
     *
     * @param tile      the tile to be added to the continent
     * @param continent the continent receiving the tile
     */
    private void addToContinent(Tile tile, Continent continent) {
        final boolean added = continent.getTiles().add(tile);
        if (added) {
            world.getTilesToContinents().put(tile, continent);
            if (!unassignedTiles.remove(tile)) {
                throw new IllegalStateException("Tile is not available to be added");
            }
        }
    }

    /**
     * Removes the given tile from the given continent and also adds it back to the collection
     * unassigned tiles. Assuming the tile is removed the continent, then
     * {@link World#getTilesToContinents()} will be updated.
     *
     * @param tile      the tile to be removed from the continent
     * @param continent the continent to have its tile removed
     */
    private void removeFromContinent(Tile tile, Continent continent) {
        final boolean removed = continent.getTiles().remove(tile);
        if (removed) {
            world.getTilesToContinents().remove(tile);
            unassignedTiles.add(tile);
        }
    }

    private void generateOceanFloor() {
        unassignedTiles.forEach(tile -> tile.setElevation(-20));

        // Make all tiles adjacent to each continent shallow, so they become coast
        for (Continent continent : world.getContinents()) {
            for (Tile tile : continent.getTiles().allAdjacents()) {
                tile.setElevation(-6);
            }
        }
    }
}
