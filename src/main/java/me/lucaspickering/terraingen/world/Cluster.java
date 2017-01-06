package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * A Cluster is a {@link Tiles}, where the tiles in the collection are assumed to form one
 * contiguous shape. With this assumption, this class can provide additional operations unique to
 * sets of tiles that are contiguous.
 *
 * Note that this class doesn't actually enforce that all of its tiles are contiguous, for
 * performance purposes. It is up to the user to only provide contiguous sets of tiles. The
 * results of this class's operations are undefined if called with disjoint tiles.
 */
public class Cluster extends Tiles {

    private Tiles world;

    // Tiles that border, but are not in, this cluster
    private Tiles adjacentTiles = new Tiles();

    private Cluster(Tiles world) {
        this.world = world;
    }

    private Cluster(Cluster cluster) {
        // Copy everything
        this.world = cluster.world;
        this.adjacentTiles = new Tiles(cluster.adjacentTiles);
    }

    public static Cluster fromWorld(Tiles world) {
        return new Cluster(world);
    }

    public static Cluster copy(Cluster cluster) {
        return new Cluster(cluster);
    }

    @Override
    public boolean add(Tile tile) {
        final boolean added = super.add(tile);

        // If the tile was added, update the set of adjacent tiles
        if (added) {
            adjacentTiles.remove(tile); // Remove this tile because it's no longer external
            // Add any adjacent tiles that aren't in this cluster
            final Collection<Tile> adjacents = world.getAdjacentTiles(tile).values();
            for (Tile adjTile : adjacents) {
                if (!contains(adjTile)) {
                    adjacentTiles.add(adjTile);
                }
            }
        }

        return added;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(); // Not supporting this for now cause I'm lazy
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
    public Tiles allAdjacents() {
        return new Tiles(adjacentTiles);
    }
}
