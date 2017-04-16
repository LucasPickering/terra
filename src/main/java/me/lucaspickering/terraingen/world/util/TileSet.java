package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.utils.GeneralFuncs;
import me.lucaspickering.utils.Pair;

/**
 * A set of {@link Tile}s. Internally, tiles are stored in a map, keyed by their position, but
 * externally this functions as a normal set of tiles would. No two tiles can have the same
 * position. Some additional map-like operations are provided, such as accessing tiles by their
 * {@link HexPoint}.
 */
public class TileSet extends HexPointSet<Tile> {

    // Internal map
    private final Map<HexPoint, Tile> map;

    public TileSet() {
        map = new TreeMap<>(); // Uses HexPoint's compareTo method for ordering
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
    protected TileSet(Map<HexPoint, Tile> map) {
        this.map = map;
    }

    /**
     * Gets the set of all tiles adjacent to the given tile.
     *
     * @param tilePos the center of the search
     * @return tiles adjacent to {@code tile}, in a direction:point map
     * @throws IllegalArgumentException if {@code tile} is not in this collection
     */
    @NotNull
    public Map<Direction, Tile> getAdjacentTiles(@NotNull HexPoint tilePos) {
        Objects.requireNonNull(tilePos);

        final Map<Direction, Tile> result = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            final HexPoint otherPoint = dir.shift(tilePos); // Get the shifted point

            // If the shifted point is in the world, add it to the map
            final Tile otherTile = getByPoint(otherPoint);
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
     * @param tilePos the tile to start counting from
     * @param range   (non-negative)
     * @return all tiles in range of the given tile
     * @throws NullPointerException     if {@code tile == null}
     * @throws IllegalArgumentException if range is negative
     */
    @NotNull
    public TileSet getTilesInRange(@NotNull HexPoint tilePos, int range) {
        Objects.requireNonNull(tilePos);
        if (range < 0) {
            throw new IllegalArgumentException(String.format("Range cannot be negative, was [%d]",
                                                             range));
        }

        final TileSet result = new TileSet();

        // Implementation from http://www.redblobgames.com/grids/hexagons/#range
        // For all possible x values in the range...
        for (int x = -range; x <= range; x++) {

            // Calculate the min and max y values that a tile in this range can have
            final int minY = Math.max(-range, -x - range);
            final int maxY = Math.min(range, -x + range);
            for (int y = minY; y <= maxY; y++) {
                // Get the tile at this point and add it to the result
                final HexPoint point = tilePos.plus(x, y, -x - y);
                final Tile otherTile = getByPoint(point);
                if (otherTile != null) {
                    result.add(otherTile);
                }
            }
        }

        return result;
    }

    /**
     * Gets the set of all tiles that are in this collection and exactly the given distance from
     * the given tile.
     *
     * @param tilePos  the center of the ring
     * @param distance the distance of the ring from the epicenter (non-negative)
     * @return a new {@link TileSet} of all tiles in this collection that are the given distance
     * from the given tile
     */
    @NotNull
    public TileSet getTilesAtDistance(@NotNull HexPoint tilePos, int distance) {
        if (distance < 0) {
            throw new IllegalArgumentException(String.format(
                "Distance must be non-negative, was [%d]", distance));
        }

        // See http://www.redblobgames.com/grids/hexagons/#rings for info on this implementation

        final TileSet result = new TileSet();

        // Special case for distance 0
        if (distance == 0) {
            final Tile tile = getByPoint(tilePos);
            if (tile != null) {
                result.add(tile);
            }
            return result;
        }

        // Step <distance> tiles southwest to get the first tile on the ring
        HexPoint point = Direction.SOUTHWEST.shift(tilePos, distance);

        // For each direction, step <distance> tiles in that direction to get one side of the ring
        for (Direction dir : Direction.values()) {
            // Get the next tile on this side of the ring
            for (int d = 0; d < distance; d++) {
                // Get the point
                final Tile otherTile = getByPoint(point);
                if (otherTile != null) {
                    result.add(otherTile);
                }
                point = dir.shift(point);
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
            final Tile firstTile = GeneralFuncs.firstFromCollection(unclusteredTiles);
            final boolean isPositive = predicate.test(firstTile); // Get its state (pos/neg)

            // Start building a cluster around this tile
            final Cluster cluster = new Cluster(this);
            // Keep track of the tiles whose adjacent tiles haven't been checked yet
            final TileSet uncheckedTiles = new TileSet();

            // Add the first tile to the cluster
            addToCluster(firstTile, cluster, uncheckedTiles, unclusteredTiles);

            // If there is still at least one tile whose adjacents haven't been checked yet...
            while (!uncheckedTiles.isEmpty()) {
                // Grab one of those unchecked tiles
                final Tile tile = GeneralFuncs.firstFromCollection(uncheckedTiles);

                // For each tile adjacent to that one...
                for (final Tile adjTile : getAdjacentTiles(tile.pos()).values()) {
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

    /**
     * Creates a shallow immutable copy of this set. The internal tiles are still mutable, but no
     * tiles can be added/removed from the copy.
     *
     * @return an shallow immutable copy of this set
     */
    public TileSet immutableCopy() {
        return new TileSet(Collections.unmodifiableMap(map));
    }
}
