package me.lucaspickering.terra.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.utils.GeneralFuncs;

/**
 * A Cluster is a {@link TileSet}, where the tiles in the collection are assumed to form one
 * contiguous shape. With this assumption, this class can provide additional operations unique to
 * sets of tiles that are contiguous.
 *
 * Note that this class doesn't actually enforce that all of its tiles are contiguous, for
 * performance purposes. It is up to the user to only provide contiguous sets of tiles. The results
 * of this class's operations are undefined if called with disjoint tiles.
 */
public class Cluster extends TileSet {

    /**
     * Constructs a new empty Cluster.
     */
    private Cluster() {
        super();
    }

    /**
     * Clusters the tiles in the given set into a list of clusters based on adjacency. For each tile
     * in any given cluster, the following is true: <ul> <li>if it is not the only tile in the
     * cluster, then it is adjacent to at least one other tile in the cluster</li> <li>it satisfies
     * the given predicate</li> </ul>
     *
     * This means that each cluster is one contiguous set of tiles. No two clusters will be adjacent
     * to each other, meaning that every cluster will be as large as possible.
     *
     * Each tile in the given set will be in no more than one cluster. If it satisfies the
     * predicate, it will be in EXACTLY ONE cluster. Otherwise, it will not be in any cluster.
     *
     * The predicate should be stable, i.e. the same input always returns the same output.
     */
    @NotNull
    public static List<Cluster> predicateCluster(@NotNull TileSet tiles,
                                                 @NotNull Predicate<Tile> predicate) {
        return categoryCluster(tiles, predicate::test).get(true);
    }

    /**
     * Clusters the tiles in the given set into lists of clusters based on adjacency. For each tile
     * in any given cluster, the following is true: <ul> <li>if it is not the only tile in the
     * cluster, then it is adjacent to at least one other tile in the cluster</li> <li>it is in the
     * same category as every other tile in the cluster, as defined by the given category
     * function</li> </ul>
     *
     * This means that each cluster is one contiguous set of tiles. No two clusters of the same
     * category will be adjacent to each other, meaning that every cluster will be as large as
     * possible.
     *
     * The returned map of clusters contains exactly one list of clusters for each category that
     * exists among the given tiles.
     *
     * Each tile in the given set will be in EXACTLY ONE of the returned clusters.
     *
     * The category function should be stable, i.e. the same input always returns the same output.
     *
     * @param tiles        the set of tiles on which to operate
     * @param categoryFunc the function used to determine if each tile should be clustered or not
     * @return the positive and negative clusters, in a pair (with positive first)
     */
    @NotNull
    public static <T> Map<T, List<Cluster>>
    categoryCluster(@NotNull TileSet tiles, @NotNull Function<Tile, T> categoryFunc) {
        // Potential optimization?
        // First divide each tile into its own cluster, then iterate over all those clusters and
        // begin joining clusters that have the same category and have adjacent tiles.

        final TileSet unclusteredTiles = new TileSet(tiles); // Copy the input structure
        final Map<T, List<Cluster>> result = new HashMap<>();

        // Each iteration of this loop creates a new cluster
        while (!unclusteredTiles.isEmpty()) {
            // Grab a tile to work with
            final Tile firstTile = GeneralFuncs.firstFromCollection(unclusteredTiles);
            final T category = categoryFunc.apply(firstTile); // Determine its category

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

                // For each unclustered tile adjacent to this one...
                for (final Tile adjTile : unclusteredTiles.getAdjacentTiles(tile.pos()).values()) {
                    // If this adjacent tile has the same category as the cluster, add it
                    if (categoryFunc.apply(adjTile).equals(category)) {
                        addToCluster(adjTile, cluster, uncheckedTiles, unclusteredTiles);
                    }
                }

                uncheckedTiles.remove(tile); // We've now checked this tile
            }

            // Add this cluster to the list of clusters in this category. If there is no list for
            // this category yet, make one and add it to the map.
            final List<Cluster> clusterList;
            if (result.containsKey(category)) {
                clusterList = result.get(category);
            } else {
                clusterList = new ArrayList<>();
                result.put(category, clusterList);
            }
            clusterList.add(cluster);
        }

        return result;
    }

    private static void addToCluster(Tile tile, Cluster cluster, TileSet uncheckedTiles,
                                     TileSet unclusteredTiles) {
        cluster.add(tile);
        uncheckedTiles.add(tile);
        unclusteredTiles.remove(tile);
    }

    /**
     * Clusters the tiles in the given set into one or more clusters. Each tile in each cluster is:
     * <ul> <li>adjacent to at least one other tile in its cluster</li> <li>similar to at least one
     * tile adjacent to it (see below for definition of "similar")</li> </ul>
     *
     * Each cluster will be one contiguous set of tiles.
     *
     * Two tiles are considered "similar" iff the similarity score between them, as determined by
     * the given similarity function, is GREATER THAN OR EQUAL TO the given similarity threshold.
     *
     * The given similarity function must be commutative ({@code f(a, b) == f(b, a)}, always) and
     * stable, i.e. {@code f(a, b)} always returns the same result, provided {@code a} and {@code b}
     * do not change.
     *
     * @param similarityFunc      the function used to determine how similar two tiles are
     * @param similarityThreshold the minimum similarity score two tiles need in order to be
     *                            considered similar to each other
     * @return the clusters
     */
    @NotNull
    public List<Cluster> similarityCluster(@NotNull ToDoubleBiFunction<Tile, Tile> similarityFunc,
                                           double similarityThreshold) {
        throw new UnsupportedOperationException(); // TODO implement if necessary
    }
}
