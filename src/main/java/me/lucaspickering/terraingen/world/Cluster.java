package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

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

    public Cluster() {
        super();
    }

    public Cluster(Tiles tiles) {
        super(tiles);
    }

    /**
     * Gets all tiles that are adjacent to this cluster. For a tile to be adjacent to the
     * cluster, it has to be:
     * <ul>
     * <li>not in the cluster</li>
     * <li>directly adjacent to one or more tiles in the cluster</li>
     * </ul>
     *
     * @return the tiles that are adjacent to, but not in, this cluster
     */
    @NotNull
    public Tiles allAdjacents() {
        // TODO make this smarter
        // Add all the adjacents for each tile in this cluster
        final Tiles result = new Tiles();
        for (Tile tile : this) {
            result.addAll(tile.adjacents().values());
        }
        result.removeAll(this); // Remove all tiles that are in this cluster
        return result;
    }
}
