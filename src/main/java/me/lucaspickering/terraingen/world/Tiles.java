package me.lucaspickering.terraingen.world;

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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * A set of {@link Tile}s. Internally, tiles are stored in a map, keyed by their position, but
 * externally this functions as a normal set of tiles would. No two tiles can have the same
 * position. Some additional map-like operations are provided, such as accessing tiles by their
 * {@link TilePoint}.
 */
public class Tiles extends AbstractSet<Tile> {

    // Internal map
    private final Map<TilePoint, Tile> map;

    public Tiles() {
        map = new HashMap<>();
    }

    /**
     * Constructs a new {@code Tiles} by copying the map in the given object. This is a shallow
     * copy, meaning the map is copied by the objects within the map are not. You can modify the
     * returned object freely, but not the objects (tiles, etc.) inside it.
     *
     * @param tiles the object to copy
     */
    public Tiles(Collection<? extends Tile> tiles) {
        this();
        addAll(tiles);
    }

    /**
     * Constructs a new {@code Tiles} backed by the given map. No copying is done.
     *
     * @param map the map to back this object
     */
    private Tiles(Map<TilePoint, Tile> map) {
        this.map = map;
    }

    /**
     * Creates a deep immutable copy of this object. Each internal tile will also be copied and
     * made immutable.
     *
     * @return the immutable copy
     */
    public Tiles immutableCopy() {
        // Turn the map of point:Tile into a map of point:ImmutableTile
        final Map<TilePoint, ImmutableTile> tiles = new HashMap<>();
        for (Tile tile : map.values()) {
            tiles.put(tile.pos(), new ImmutableTile(tile));
        }
        return new Tiles(Collections.unmodifiableMap(tiles));
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
        if (!map.containsKey(point)) {
            throw new IllegalArgumentException("Tile is not in the world");
        }

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
     * @throws IllegalArgumentException if {@code tile} is not in this collection or range is
     *                                  negative
     */
    @NotNull
    public Tiles getTilesInRange(@NotNull Tile tile, int range) {
        Objects.requireNonNull(tile);
        final TilePoint point = tile.pos();
        if (!map.containsKey(point)) {
            throw new IllegalArgumentException("Tile is not in the world");
        }
        if (range < 0) {
            throw new IllegalArgumentException(String.format("Range must be positive, was [%d]",
                                                             range));
        }

        final Tiles result = new Tiles();
        result.add(tile); // The result always has the tile in it

        // Add everything other than the tile
        Tiles lastAdjacents = new Tiles(result);
        for (int i = 1; i <= range; i++) {
            // Start with tiles directly adjacent to this one
            final Tiles adjacents = new Tiles();
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
    public Tiles selectTiles(Random random, int numToPick, int minSpacing) {
        // Copy the tiles because we're going to be modifying it
        final Tiles candidates = new Tiles(this);
        final Tiles result = new Tiles(); // The tiles that will be returnec

        // While we haven't hit our target number and there are tiles left to pick...
        while (result.size() < numToPick && !candidates.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final Tile tile = Funcs.randomFromCollection(random, candidates);
            result.add(tile); // Add it to the collection

            // If we need spacing, remove nearby tiles
            if (minSpacing > 0) {
                // Get all the tiles that are too close to this one to be selected themselves,
                // and remove them from the set of candidates
                final Tiles tooClose = getTilesInRange(tile, minSpacing);
                tooClose.forEach(candidates::remove); // Remove each tile
            }
        }

        return result;
    }

    /**
     * Clusters the tiles in this collection into two sets of clusters. Each tile in each cluster is
     * adjacent to at least one other tile in the cluster, so that each cluster is one contiguous
     * set of tiles.
     *
     * The returned pair of clusters contains both the positive and negative clusters. For the
     * isPositive clusters, each tile in each cluster <i>satisfies</i> the predicate. For the
     * negative clusters, each tile in each cluster <i>does not satisy</i> the predicate.
     *
     * Each tile in the given map will be in EXACTLY ONE of the returned clusters.
     *
     * @param predicate the function used to determine if each tile should be clustered or not
     * @return the isPositive and negative clusters, in a pair (with isPositive first)
     */
    @NotNull
    public Pair<List<Cluster>, List<Cluster>> clusterTiles(@NotNull Predicate<Tile> predicate) {
        // First divided each tile into its own cluster, then iterate over all those clusters and
        // begin joining clusters that have the same state (postive or negative) and have
        // adjacent tiles.

        final Tiles unclusteredTiles = new Tiles(this); // Copy the input structure
        final List<Cluster> posClusters = new LinkedList<>(); // These satisfy the predicate
        final List<Cluster> negClusters = new LinkedList<>(); // These DON'T satisfy the predicate

        // Each iteration of this loop creates a new cluster
        while (!unclusteredTiles.isEmpty()) {
            // Grab a tile to work with
            final Tile firstTile = Funcs.firstFromCollection(unclusteredTiles);
            final boolean positive = predicate.test(firstTile); // Get its state (pos/neg)

            // Start building a cluster around this tile
            final Cluster cluster = Cluster.fromWorld(this);
            // Keep track of the tiles whose adjacent tiles haven't been checked yet
            final Tiles uncheckedTiles = new Tiles();

            // Add the first tile to the cluster, and remove it from unclusteredTiles
            cluster.add(firstTile);
            uncheckedTiles.add(firstTile);
            unclusteredTiles.remove(firstTile);

            // If there is still at least one tile whose adjacents haven't been checked yet...
            while (!uncheckedTiles.isEmpty()) {
                // Grab one of those unchecked tiles
                final Tile tile = Funcs.firstFromCollection(uncheckedTiles);

                // For each tile adjacent to that one...
                for (final Tile adjTile : getAdjacentTiles(tile).values()) {
                    // If this adjacent tile has the same pos/neg state, and it's not already in
                    // the cluster...
                    if (predicate.test(adjTile) == positive
                        && !cluster.contains(adjTile)) {
                        // Add the first tile to the cluster, and remove it from unclusteredTiles
                        cluster.add(adjTile);
                        uncheckedTiles.add(adjTile);
                        unclusteredTiles.remove(adjTile);
                    }
                }

                uncheckedTiles.remove(tile); // We've now checked this tile
            }

            if (positive) {
                posClusters.add(cluster);
            } else {
                negClusters.add(cluster);
            }
        }

        return new Pair<>(posClusters, negClusters);
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
    public List<Cluster> clusterTiles(@NotNull BiFunction<Tile, Tile, Double> similarityFunc,
                                      double similarityThreshold) {
        throw new UnsupportedOperationException(); // TODO implement if necessary
    }
}
