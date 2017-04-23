package me.lucaspickering.terra.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.Tile;

/**
 * A set of {@link Tile}s. Internally, tiles are stored in a map, keyed by their position, but
 * externally this functions as a normal set of tiles would. No two tiles can have the same
 * position. Some additional map-like operations are provided, such as accessing tiles by their
 * {@link HexPoint}.
 */
public class TileSet extends HexPointSet<Tile> {

    public TileSet() {
        super();
    }

    /**
     * Constructs a new {@link TileSet} by copying the values in the given object. This is a shallow
     * copy, meaning the objects are put in this collection without being copied. You can modify the
     * returned object freely, but be careful about modifying the tiles inside it.
     *
     * @param tiles the object to copy
     */
    public TileSet(Collection<? extends Tile> tiles) {
        super(tiles);
    }

    /**
     * Constructs a new {@link TileSet} backed by the given map. No copying is done.
     *
     * @param map the map to back this object
     */
    protected TileSet(Map<HexPoint, Tile> map) {
        super(map);
    }

    /**
     * Gets the set of all tiles adjacent to the given tile.
     *
     * @param tilePos the center of the search
     * @return tiles adjacent to {@code tile}, in a direction:point map
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
     * Creates a shallow immutable copy of this set. The internal tiles are still mutable, but no
     * tiles can be added/removed from the copy.
     *
     * @return an shallow immutable copy of this set
     */
    public TileSet immutableCopy() {
        return new TileSet(immutableInternalMap());
    }
}
