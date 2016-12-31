package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldHelper {

    /**
     * Converts a {@link TilePoint} in the world to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link TilePoint}
     * @return the position of that tile's getCenter on the screen
     */
    @NotNull
    public static Point tileToPixel(@NotNull TilePoint tile) {
        final float x = Tile.WIDTH * tile.x() * 0.75f;
        final float y = -Tile.HEIGHT * (tile.x() / 2.0f + tile.y());
        return new Point((int) x, (int) y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link TilePoint} in the world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there. The given point needs to be shifted based on the world getCenter before calling
     * this function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public static TilePoint pixelToTile(@NotNull Point pos) {
        // Convert it to a fractional tile point
        final float fracX = pos.x() * 2f / 3f / Tile.RADIUS;
        final float fracY = -(pos.x() + (float) Math.sqrt(3) * pos.y()) / (Tile.RADIUS * 3f);
        final float fracZ = -fracX - fracY; // We'll need this later

        // Convert the fraction tile coordinates to regular coordinates
        // First, get rounded versions of each coord
        int roundX = Math.round(fracX);
        int roundY = Math.round(fracY);
        int roundZ = Math.round(fracZ);

        // roundX + roundY + roundZ == 0 is not guaranteed, so we need to recalculate one of them

        // Find how much each one needed to be rounded
        final float xDiff = Math.abs(fracX - roundX);
        final float yDiff = Math.abs(fracY - roundY);
        final float zDiff = Math.abs(fracZ - roundZ);

        // Recalculate the one that rounded the most
        if (xDiff > yDiff && xDiff > zDiff) {
            roundX = -roundY - roundZ;
        } else if (yDiff > zDiff) {
            roundY = -roundX - roundZ;
        } else {
            roundZ = -roundX - roundY;
        }

        return new TilePoint(roundX, roundY, roundZ);
    }

    /**
     * Gets the set of all tiles adjacent to the given tile that exist in the world.
     *
     * @param tiles  all tiles in the world
     * @param origin the center of the search
     * @return tiles adjacent to {@code origin}, in a direction:point map
     * @throws IllegalArgumentException if {@code origin} is not in {@code world}
     */
    @NotNull
    public static Map<Direction, TilePoint> getAdjacentTiles(@NotNull Tiles tiles,
                                                             @NotNull TilePoint origin) {
        Objects.requireNonNull(origin);
        if (!tiles.containsKey(origin)) {
            throw new IllegalArgumentException("Origin is not in the world");
        }

        final Map<Direction, TilePoint> result = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            final TilePoint point = dir.shift(origin); // Get the shifted point

            // If the shifted point is in the world, add it to the map
            if (tiles.containsKey(point)) {
                result.put(dir, point);
            }
        }

        return result;
    }

    /**
     * Gets all tile points in the given range of the given point. A tile will be included in
     * the output if it exists in the world, and it is within {@code range} steps of {@code
     * origin}. For example, giving a range of 0 returns just the origin, 1 returns the origin
     * and all adjacent tiles, etc.
     *
     * @param tiles  all tiles in the world
     * @param origin the tile to start counting from
     * @param range  (non-negative)
     * @return all tiles in range of the given origin
     * @throws NullPointerException     if {@code origin == null}
     * @throws IllegalArgumentException if {@code origin} is not in {@code world} or {@code range <
     *                                  0}
     */
    @NotNull
    public static Set<TilePoint> getTilesInRange(@NotNull Tiles tiles,
                                                 @NotNull TilePoint origin, int range) {
        Objects.requireNonNull(origin);
        if (!tiles.containsKey(origin)) {
            throw new IllegalArgumentException("Origin is not in the world");
        }
        if (range < 0) {
            throw new IllegalArgumentException(String.format("Range must be isPositive, was [%d]",
                                                             range));
        }

        final Set<TilePoint> result = new HashSet<>();
        result.add(origin); // The result always has the origin in it

        // Add everything other than the origin
        Set<TilePoint> lastAdjacents = new HashSet<>(result);
        for (int i = 1; i <= range; i++) {
            // Start with tiles directly adjacent to this one
            final Set<TilePoint>
                adjacents =
                new HashSet<>(getAdjacentTiles(tiles, origin).values());
            for (TilePoint adjacent : lastAdjacents) {
                adjacents.addAll(getAdjacentTiles(tiles, adjacent).values());
            }

            result.addAll(adjacents);
            lastAdjacents = adjacents;
        }

        return result;
    }

    /**
     * Randomly selects the specified number of tiles from the given {@link Tiles}. If necessary,
     * this ensures a minimum spacing between selections. If that spacing is 0, nothing in
     * enforced. If it is 1, it makes sure that no two selections are adjacent, and so on.
     *
     * If there are enough tiles in the collection to select as many as requested while keeping the
     * requested spacing, then fewer tiles will be returned. Note that fewer than the requrested
     * number can be returned even if the requested number is attainable. This could happen if
     * the function randomly chooses a suboptimal spread of tiles and forces itself out of space.
     *
     * @param tiles      the set of tiles to choose from
     * @param random     the random instance to use
     * @param numToPick  the amount of tiles to select
     * @param minSpacing the minimum amount of tiles separating selections, or 0 for no separation
     * @return the randomly-selected tiles
     */
    @NotNull
    public static Tiles selectTiles(Tiles tiles, Random random, int numToPick, int minSpacing) {
        // Copy the tiles because we're going to be modifying it
        final Tiles candidates = new Tiles(tiles);
        final Tiles result = new Tiles(); // The tiles that will be returnec

        // While we haven't hit our target number and there are tiles left to pick...
        while (result.size() < numToPick && !candidates.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final Tile tile = Funcs.randomFromCollection(random, candidates.values());
            result.putTile(tile); // Add it to the collection

            // If we need spacing, remove nearby tiles
            if (minSpacing > 0) {
                // Get all the tiles that are too close to this one to be selected themselves,
                // and remove them from the set of candidates
                final Set<TilePoint> tooClose = WorldHelper.getTilesInRange(tiles, tile.pos(),
                                                                            minSpacing);
                tooClose.forEach(candidates::remove); // Remove each tile
            }
        }

        return result;
    }

    /**
     * Clusters the given tiles into two sets of clusters. Each tile in each cluster is adjacent
     * to at least one other tile in the cluster, so that each cluster is one contiguous set of
     * tiles.
     *
     * The returned pair of clusters contains both the isPositive and negative clusters. For the
     * isPositive clusters, each tile in each cluster <i>satisfies</i> the predicate. For the
     * negative clusters, each tile in each cluster <i>does not satisy</i> the predicate.
     *
     * Each tile in the given map will be in EXACTLY ONE of the returned clusters.
     *
     * @param tiles     the tiles to cluster
     * @param predicate the function used to determine if each tile should be clustered or not
     * @return the isPositive and negative clusters, in a pair (with isPositive first)
     */
    @NotNull
    public static Pair<List<Tiles>, List<Tiles>> clusterTiles(@NotNull Tiles tiles,
                                                              @NotNull Predicate<Tile> predicate) {
        // First divided each tile into its own cluster, then iterate over all those clusters and
        // begin joining clusters that have the same state (postive or negative) and have
        // adjacent tiles.

        final Tiles unclusteredTiles = new Tiles(tiles); // Copy the input structure
        final List<Tiles> posClusters = new LinkedList<>(); // These satisfy the predicate
        final List<Tiles> negClusters = new LinkedList<>(); // These DON'T satisfy the predicate

        // Each iteration of this loop creates a new cluster
        while (!unclusteredTiles.isEmpty()) {
            // Grab a tile to work with
            final Tile firstTile = Funcs.firstFromCollection(unclusteredTiles.values());
            final boolean positive = predicate.test(firstTile); // Get its state (pos/neg)

            // Start building a cluster around this tile
            final Tiles cluster = new Tiles();
            // Keep track of the tiles whose adjacent tiles haven't been checked yet
            final Tiles uncheckedTiles = new Tiles();

            // Add the first tile to the cluster, and remove it from unclusteredTiles
            cluster.putTile(firstTile);
            uncheckedTiles.putTile(firstTile);
            unclusteredTiles.remove(firstTile.pos());

            // If there is still at least one tile whose adjacents haven't been checked yet...
            while (!uncheckedTiles.isEmpty()) {
                // Grab one of those unchecked tiles
                final Tile tile = Funcs.firstFromCollection(uncheckedTiles.values());

                // For each tile adjacent to that one...
                for (final Tile adjTile : tile.adjacents().values()) {
                    // If this adjacent tile has the same pos/neg state, and it's not already in
                    // the cluster...
                    if (predicate.test(adjTile) == positive
                        && !cluster.containsKey(adjTile.pos())) {
                        // Add the first tile to the cluster, and remove it from unclusteredTiles
                        cluster.putTile(adjTile);
                        uncheckedTiles.putTile(adjTile);
                        unclusteredTiles.remove(adjTile.pos(), adjTile);
                    }
                }

                uncheckedTiles.remove(tile.pos()); // We've now checked this tile
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
     * Clusters the given tiles into one or more clusters. Each tile in each cluster is:
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
     * @param tiles               the tiles to cluster
     * @param similarityFunc      the function used to determine how similar two tiles are (should
     *                            be commutative)
     * @param similarityThreshold the minimum similarity score two tiles need in order to be
     *                            considered similar to each other
     * @return the clusters
     */
    @NotNull
    public static List<Tiles> clusterTiles(@NotNull Tiles tiles,
                                           @NotNull BiFunction<Tile, Tile, Double> similarityFunc,
                                           double similarityThreshold) {
        throw new UnsupportedOperationException(); // TODO implement if necessary
    }
}
