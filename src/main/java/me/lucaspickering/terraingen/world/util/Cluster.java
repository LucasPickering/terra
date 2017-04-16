package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import me.lucaspickering.terraingen.world.Tile;

/**
 * A Cluster is a {@link TileSet}, where the tiles in the collection are assumed to form one
 * contiguous shape. With this assumption, this class can provide additional operations unique to
 * sets of tiles that are contiguous.
 *
 * Note that this class doesn't actually enforce that all of its tiles are contiguous, for
 * performance purposes. It is up to the user to only provide contiguous sets of tiles. The
 * results of this class's operations are undefined if called with disjoint tiles.
 *
 * Each cluster exists in a world, which is meant to be a super set of the tiles in the cluster.
 * This is never enforced by the structure, for efficiency purposes, but all operations of this
 * class are undefined if you add a tile to the cluster that is not in its world.
 */
public class Cluster extends TileSet {

    private TileSet world;

    // Tiles that border, but are not in, this cluster
    private TileSet adjacentTiles = new TileSet();

    /**
     * Creates a new {@link Cluster} that exists as a subset of the given world.
     *
     * @param world the world of tiles that is the superset of the returned {@link Cluster}
     */
    public Cluster(TileSet world) {
        this.world = world;
    }

    /**
     * Copies the given {@link Cluster}, but uses a new {@link TileSet} as the world.
     *
     * @param cluster the cluster to copy
     * @param world   the new world for the cluster to exist in
     */
    public Cluster(Cluster cluster, TileSet world) {
        this(world);
        addAll(cluster);
    }

    @Override
    public boolean add(Tile tile) {
        final boolean added = super.add(tile);

        // If the tile was added, update the set of adjacent tiles
        if (added) {
            adjacentTiles.remove(tile); // Remove this tile because it's no longer external
            // Add any adjacent tiles that aren't in this cluster
            final Collection<Tile> adjacents = world.getAdjacentTiles(tile.pos()).values();
            for (Tile adjTile : adjacents) {
                if (!contains(adjTile)) {
                    adjacentTiles.add(adjTile);
                }
            }
        }

        return added;
    }

    @Override
    public boolean removeByPoint(HexPoint point) {
        final boolean removed = super.removeByPoint(point);

        if (removed) {
            final Tile tile = world.getByPoint(point);
            // For each tile adjacent to the removed one...
            for (Tile adjTile : adjacentTiles.getAdjacentTiles(tile.pos()).values()) {
                // If there are no more tiles in the cluster adjacent to this one, then it is no
                // longer adjacent to the cluster and should therefore be removed from that set
                if (getAdjacentTiles(adjTile.pos()).isEmpty()) {
                    adjacentTiles.remove(adjTile);
                }
            }
            adjacentTiles.add(tile);
        }

        return removed;
    }

    /**
     * Gets all tiles that are adjacent to this cluster. For a tile to be adjacent to the
     * cluster, it has to be:
     * <ul>
     * <li>not in the cluster</li>
     * <li>directly adjacent to one or more tiles in the cluster</li>
     * </ul>
     *
     * The returned collection is a copy of the one held internally, so it can be freely modified.
     * Its contents however are not copied so modify the internal objects at your own risk.
     *
     * @return the tiles that are adjacent to, but not in, this cluster
     */
    @NotNull
    public TileSet allAdjacents() {
        // Potential optimization?
        return new TileSet(adjacentTiles);
    }
}
