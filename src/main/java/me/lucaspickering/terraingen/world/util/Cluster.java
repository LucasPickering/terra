package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.utils.GeneralFuncs;
import me.lucaspickering.utils.Pair;

/**
 * A Cluster is a {@link TileSet}, where the tiles in the collection are assumed to form one
 * contiguous shape. With this assumption, this class can provide additional operations unique to
 * sets of tiles that are contiguous.
 *
 * Note that this class doesn't actually enforce that all of its tiles are contiguous, for
 * performance purposes. It is up to the user to only provide contiguous sets of tiles. The
 * results of this class's operations are undefined if called with disjoint tiles.
 */
public class Cluster extends TileSet {

    /**
     * Constructs a new empty Cluster.
     */
    private Cluster() {
        super();
    }

    /**
     * Clusters the tiles in the given set based on adjacency. Each tile in each cluster is
     * adjacent to at least one other tile in the cluster, so that each cluster is one contiguous
     * set of tiles. The clusters are also disjoint, so that no two clusters are adjacent to each
     * other.
     *
     * In other words, this groups all tiles in this collection into contiguous clusters that are
     * each as large as possible.
     *
     * Each tile in the given set will be in EXACTLY ONE of the returned clusters.
     *
     * @return the cluster
     */
    @NotNull
    public static List<Cluster> cluster(@NotNull TileSet tiles) {
        return cluster(tiles, tile -> true).first();
    }

    /**
     * Clusters the tiles in the given set into two sets of clusters based on adjacency. Each tile
     * in each cluster is adjacent to at least one other tile in the cluster, so that each
     * cluster is one contiguous set of tiles.
     *
     * The returned pair of clusters contains both the positive and negative clusters. For the
     * positive clusters, each tile in each cluster <i>satisfies</i> the predicate. For the
     * negative clusters, each tile in each cluster <i>does not satisy</i> the predicate. Two
     * positive clusters cannot be adjacent to each other, and the same goes for negative
     * clusters. This means that each cluster will be as large as possible.
     *
     * Each tile in the given map will be in EXACTLY ONE of the returned clusters.
     *
     * @param predicate the function used to determine if each tile should be clustered or not
     * @return the positive and negative clusters, in a pair (with positive first)
     */
    @NotNull
    public static Pair<List<Cluster>, List<Cluster>> cluster(@NotNull TileSet tiles,
                                                             @NotNull Predicate<Tile> predicate) {
        // Potential optimization?
        // First divide each tile into its own cluster, then iterate over all those clusters and
        // begin joining clusters that have the same state (postive or negative) and have
        // adjacent tiles.

        final TileSet unclusteredTiles = new TileSet(tiles); // Copy the input structure
        final List<Cluster> posClusters = new LinkedList<>(); // These satisfy the predicate
        final List<Cluster> negClusters = new LinkedList<>(); // These DON'T satisfy the predicate

        // Each iteration of this loop creates a new cluster
        while (!unclusteredTiles.isEmpty()) {
            // Grab a tile to work with
            final Tile firstTile = GeneralFuncs.firstFromCollection(unclusteredTiles);
            final boolean isPositive = predicate.test(firstTile); // Get its state (pos/neg)

            // Start building a cluster around this tile
            final Cluster cluster = new Cluster();
            // Keep track of the tiles whose adjacent tiles haven't been checked yet
            final TileSet uncheckedTiles = new TileSet();

            // Add the first tile to the cluster
            addToCluster(firstTile, cluster, uncheckedTiles, unclusteredTiles);

            // If there is still at least one tile whose adjacents haven't been checked yet...
            while (!uncheckedTiles.isEmpty()) {
                // Grab one of those unchecked tiles
                final Tile tile = GeneralFuncs.firstFromCollection(uncheckedTiles);

                // For each tile adjacent to that one...
                for (final Tile adjTile : tiles.getAdjacentTiles(tile.pos()).values()) {
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

    /**
     * Clusters the tiles in the given set into one or more clusters. Each tile in each cluster:
     * <ul>
     * <li>is adjacent to at least one other tile in its cluster</li>
     * <li>is similar to at least one tile adjacent to it</li>
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
    public List<Cluster> cluster(@NotNull ToDoubleBiFunction<Tile, Tile> similarityFunc,
                                 double similarityThreshold) {
        throw new UnsupportedOperationException(); // TODO implement if necessary
    }

    private static void addToCluster(Tile tile, Cluster cluster, TileSet uncheckedTiles,
                                     TileSet unclusteredTiles) {
        cluster.add(tile);
        uncheckedTiles.add(tile);
        unclusteredTiles.remove(tile);
    }
}
