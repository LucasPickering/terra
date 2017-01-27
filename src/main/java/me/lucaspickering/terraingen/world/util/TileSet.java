package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * A set of {@link Tile}s. Internally, tiles are stored in a map, keyed by their position, but
 * externally this functions as a normal set of tiles would. No two tiles can have the same
 * position. Some additional map-like operations are provided, such as accessing tiles by their
 * {@link TilePoint}.
 */
public class TileSet extends AbstractSet<Tile> {

    // Internal map
    private final Map<TilePoint, Tile> map;

    public TileSet() {
        map = new TreeMap<>(); // Uses TilePoint's compareTo method for ordering
    }

    /**
     * Constructs a new {@link TileSet} by copying the values in the given object. This is a shallow
     * copy, meaning the objects are put in this collection without being copied. You can modify the
     * returned object freely, but be careful about modifying the tiles inside it.
     *
     * @param tiles the object to copy
     */
    public TileSet(Collection<? extends Tile> tiles) {
        this();
        addAll(tiles);
    }

    /**
     * Constructs a new {@link TileSet} backed by the given map. No copying is done.
     *
     * @param map the map to back this object
     */
    private TileSet(Map<TilePoint, Tile> map) {
        this.map = map;
    }

    /**
     * Initializes a {@link TileSet} collection of the given radius. Each tile will have default
     * biome and elevation. The returned {@link TileSet} will have an origin tile and {@code
     * radius} rings of tiles around that origin.
     *
     * @param radius the radius of the collection of tiles
     * @return the initialized {@link TileSet}
     */
    @NotNull
    public static TileSet initByRadius(int radius) {
        final TileSet tiles = new TileSet();
        // Fill out the set with a bunch of points
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x + y + z == 0) {
                        tiles.add(new Tile(new TilePoint(x, y, z)));
                    }
                }
            }
        }
        return tiles;
    }

    /**
     * Creates a deep immutable copy of this object. Each internal tile will also be copied and
     * made immutable.
     *
     * @return the immutable copy
     */
    public TileSet immutableCopy() {
        // Turn the map of point:Tile into a map of point:ImmutableTile
        final Map<TilePoint, ImmutableTile> tiles = new HashMap<>();
        for (Tile tile : map.values()) {
            tiles.put(tile.pos(), new ImmutableTile(tile));
        }
        return new TileSet(Collections.unmodifiableMap(tiles));
    }

    public Tile getByPoint(TilePoint point) {
        return map.get(point);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        // Cast o to a tile and check if its position is in the map
        return o instanceof Tile && containsPoint(((Tile) o).pos());
    }

    public boolean containsPoint(TilePoint point) {
        return map.containsKey(point);
    }

    @NotNull
    @Override
    public Iterator<Tile> iterator() {
        return map.values().iterator();
    }

    @Override
    public boolean add(Tile tile) {
        Objects.requireNonNull(tile);
        map.put(tile.pos(), tile);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Tile) {
            final Tile tile = (Tile) o;
            return removePoint(tile.pos());
        }
        return false;
    }

    public boolean removePoint(TilePoint point) {
        return map.remove(point) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Gets the set of all tiles adjacent to the given tile.
     *
     * @param tile the center of the search
     * @return tiles adjacent to {@code tile}, in a direction:point map
     * @throws IllegalArgumentException if {@code tile} is not in this collection
     */
    @NotNull
    public Map<Direction, Tile> getAdjacentTiles(@NotNull Tile tile) {
        Objects.requireNonNull(tile);
        final TilePoint point = tile.pos();

        final Map<Direction, Tile> result = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            final TilePoint otherPoint = dir.shift(point); // Get the shifted point

            // If the shifted point is in the world, add it to the map
            final Tile otherTile = map.get(otherPoint);
            if (otherTile != null) {
                result.put(dir, otherTile);
            }
        }

        return result;
    }

    /**
     * Gets all tile points in the given range of the given tile. A tile will be included in
     * the output if it is in this collection, and it is within {@code range} steps of {@code
     * tile}. For example, giving a range of 0 returns just the given tile, 1 returns the tile
     * and all adjacent tiles, etc.
     *
     * @param tile  the tile to start counting from
     * @param range (non-negative)
     * @return all tiles in range of the given tile
     * @throws NullPointerException     if {@code tile == null}
     * @throws IllegalArgumentException if range is negative
     */
    @NotNull
    public TileSet getTilesInRange(@NotNull Tile tile, int range) {
        Objects.requireNonNull(tile);
        if (range < 0) {
            throw new IllegalArgumentException(String.format("Range must be positive, was [%d]",
                                                             range));
        }

        final TileSet result = new TileSet();
        result.add(tile); // The result always has the tile in it

        // Add everything other than the tile
        TileSet lastAdjacents = new TileSet(result);
        for (int i = 1; i <= range; i++) {
            // Start with tiles directly adjacent to this one
            final TileSet adjacents = new TileSet();
            for (Tile adjacent : lastAdjacents) {
                adjacents.addAll(getAdjacentTiles(adjacent).values());
            }

            result.addAll(adjacents);
            lastAdjacents = adjacents;
        }

        return result;
    }

    /**
     * Randomly selects the specified number of tiles from this collection. If necessary,
     * this ensures a minimum spacing between selections. If that spacing is 0, nothing in
     * enforced. If it is 1, it makes sure that no two selections are adjacent, and so on.
     *
     * If there are enough tiles in the collection to select as many as requested while keeping the
     * requested spacing, then fewer tiles will be returned. Note that fewer than the requested
     * number may be returned even if the requested number is attainable. This could happen if
     * the function randomly chooses a suboptimal spread of tiles and forces itself out of space.
     *
     * @param random     the random instance to use
     * @param numToPick  the amount of tiles to select
     * @param minSpacing the minimum amount of tiles separating selections, or 0 for no separation
     * @return the randomly-selected tiles
     */
    @NotNull
    public TileSet selectTiles(Random random, int numToPick, int minSpacing) {
        // Copy the tiles because we're going to be modifying it
        final TileSet candidates = new TileSet(this);
        final TileSet result = new TileSet(); // The tiles that will be returnec

        // While we haven't hit our target number and there are tiles left to pick...
        while (result.size() < numToPick && !candidates.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final Tile tile = Funcs.randomFromCollection(random, candidates);
            result.add(tile); // Add it to the collection

            // If we need spacing, remove nearby tiles
            if (minSpacing > 0) {
                // Get all the tiles that are too close to this one to be selected themselves,
                // and remove them from the set of candidates
                final TileSet tooClose = getTilesInRange(tile, minSpacing);
                tooClose.forEach(candidates::remove); // Remove each tile
            }
        }

        return result;
    }

    /**
     * Clusters the tiles in this collection based on adjacency. Each tile in each cluster is
     * adjacent to at least one other tile in the cluster, so that each cluster is one contiguous
     * set of tiles. The clusters are also disjoint, so that no two clusters are adjacent to each
     * other.
     *
     * In other words, this groups all tiles in this collection into contiguous clusters that are
     * each as large as possible.
     *
     * Each tile in the given map will be in EXACTLY ONE of the returned clusters.
     *
     * @return the cluster
     */
    public List<Cluster> cluster() {
        return cluster(tile -> true).first();
    }

    /**
     * Clusters the tiles in this collection into two sets of clusters based on adjacency. Each tile
     * in each cluster is adjacent to at least one other tile in the cluster, so that each
     * cluster is one contiguous set of tiles.
     *
     * The returned pair of clusters contains both the positive and negative clusters. For the
     * isPositive clusters, each tile in each cluster <i>satisfies</i> the predicate. For the
     * negative clusters, each tile in each cluster <i>does not satisy</i> the predicate. Two
     * clusters of the same state (positive or negative) will not be adjacent to each other. In
     * other words, each cluster will be as big as possible.
     *
     * Each tile in the given map will be in EXACTLY ONE of the returned clusters.
     *
     * @param predicate the function used to determine if each tile should be clustered or not
     * @return the isPositive and negative clusters, in a pair (with isPositive first)
     */
    @NotNull
    public Pair<List<Cluster>, List<Cluster>> cluster(@NotNull Predicate<Tile> predicate) {
        // Potential optimization?
        // First divided each tile into its own cluster, then iterate over all those clusters and
        // begin joining clusters that have the same state (postive or negative) and have
        // adjacent tiles.

        final TileSet unclusteredTiles = new TileSet(this); // Copy the input structure
        final List<Cluster> posClusters = new LinkedList<>(); // These satisfy the predicate
        final List<Cluster> negClusters = new LinkedList<>(); // These DON'T satisfy the predicate

        // Each iteration of this loop creates a new cluster
        while (!unclusteredTiles.isEmpty()) {
            // Grab a tile to work with
            final Tile firstTile = Funcs.firstFromCollection(unclusteredTiles);
            final boolean isPositive = predicate.test(firstTile); // Get its state (pos/neg)

            // Start building a cluster around this tile
            final Cluster cluster = Cluster.fromWorld(this);
            // Keep track of the tiles whose adjacent tiles haven't been checked yet
            final TileSet uncheckedTiles = new TileSet();

            // Add the first tile to the cluster
            addToCluster(firstTile, cluster, uncheckedTiles, unclusteredTiles);

            // If there is still at least one tile whose adjacents haven't been checked yet...
            while (!uncheckedTiles.isEmpty()) {
                // Grab one of those unchecked tiles
                final Tile tile = Funcs.firstFromCollection(uncheckedTiles);

                // For each tile adjacent to that one...
                for (final Tile adjTile : getAdjacentTiles(tile).values()) {
                    // If this adjacent tile has the same pos/neg state, and it's not already in
                    // the cluster...
                    if (predicate.test(adjTile) == isPositive && !cluster.contains(adjTile)) {
                        // Add the tile to the cluster
                        addToCluster(adjTile, cluster, uncheckedTiles, unclusteredTiles);
                    }
                }

                uncheckedTiles.remove(tile); // We've now checked this tile
            }

            if (isPositive) {
                posClusters.add(cluster);
            } else {
                negClusters.add(cluster);
            }
        }

        return new Pair<>(posClusters, negClusters);
    }

    private void addToCluster(Tile tile, Cluster cluster, TileSet uncheckedTiles,
                              TileSet unclusteredTiles) {
        cluster.add(tile);
        uncheckedTiles.add(tile);
        unclusteredTiles.remove(tile);
    }

    /**
     * Clusters the tiles in this collection into one or more clusters. Each tile in each cluster
     * is:
     * <ul>
     * <li>adjacent to at least one other tile in the cluster</li>
     * <li>similar to at least one tile adjacent to it</li>
     * </ul>
     *
     * Each cluster will be one contiguous set of tiles.
     *
     * Two tiles are considered "similar" iff the similarity score between them, as determined by
     * the given similarity function, is GREATER THAN OR EQUAL TO the given similarity threshold.
     *
     * @param similarityFunc      the function used to determine how similar two tiles are (should
     *                            be commutative)
     * @param similarityThreshold the minimum similarity score two tiles need in order to be
     *                            considered similar to each other
     * @return the clusters
     */
    @NotNull
    public List<Cluster> cluster(@NotNull BiFunction<Tile, Tile, Double> similarityFunc,
                                 double similarityThreshold) {
        throw new UnsupportedOperationException(); // TODO implement if necessary
    }
}
