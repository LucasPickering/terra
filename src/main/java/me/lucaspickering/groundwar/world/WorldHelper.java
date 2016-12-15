package me.lucaspickering.groundwar.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Direction;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public class WorldHelper {

    /**
     * Converts a {@link TilePoint} in the world to a {@link Point} on the screen.
     *
     * @param pos the position of the tile as a {@link TilePoint}
     * @return the position of that tile's center on the screen
     */
    public static Point screenPosFromTilePos(TilePoint pos) {
        final float x = Tile.TILE_WIDTH * pos.x() * 0.75f;
        final float y = -Tile.TILE_HEIGHT * (pos.x() / 2.0f + pos.y());
        return Constants.WORLD_CENTER.plus((int) x, (int) y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link TilePoint} in the world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    public static TilePoint tilePosFromScreenPos(Point pos) {
        // todo fix
        final Point shiftedPos = pos.minus(Constants.WORLD_CENTER);
        final float x = shiftedPos.x() / (Tile.TILE_WIDTH * 0.75f);
        final float y = -x / 2 - shiftedPos.y() / Tile.TILE_HEIGHT;
        return new TilePoint(Math.round(x), Math.round(y));
    }

    /**
     * Gets the set of all tiles adjacent to the given tile that exist in the world.
     *
     * @param world  the set of tiles in the world
     * @param origin the center of the search
     * @return tiles adjacent to {@code origin}
     * @throws IllegalArgumentException if {@code origin} is not in {@code world}
     */
    public static Set<TilePoint> getAdjacentTiles(Set<TilePoint> world, TilePoint origin) {
        Objects.requireNonNull(origin);
        if (!world.contains(origin)) {
            throw new IllegalArgumentException("Origin is not in the world");
        }

        // Collect all tiles that are adjacent to origin and also in the world
        return Arrays.stream(Direction.values())
            .map(dir -> origin.plus(dir.delta())) // Map each direction to a point in that direction
            .filter(world::contains) // Filter out tiles that aren't in the world
            .collect(Collectors.toSet()); // Collect into a set
    }

    /**
     * Gets all tile points in the given range of the given point. A tile will be included in
     * the output if it exists in the world, and it is within {@code range} steps of {@code
     * origin}. For example, giving a range of 0 returns just the origin, 1 returns the origin
     * and all adjacent tiles, etc.
     *
     * @param world  all tile points in the world
     * @param origin the tile to start counting from
     * @param range  (non-negative)
     * @return all tiles in range of the given origin
     * @throws NullPointerException     if {@code origin == null}
     * @throws IllegalArgumentException if {@code origin} is not in {@code world} or {@code range <
     *                                  0}
     */
    public static Set<TilePoint> getTilesInRange(Set<TilePoint> world, TilePoint origin,
                                                 int range) {
        Objects.requireNonNull(origin);
        if (!world.contains(origin)) {
            throw new IllegalArgumentException("Origin is not in the world");
        }
        if (range < 0) {
            throw new IllegalArgumentException(String.format("Range must be positive, was [%d]",
                                                             range));
        }

        final Set<TilePoint> result = new HashSet<>();
        result.add(origin); // The result always has the origin in it

        // Add everything other than the origin
        Set<TilePoint> lastAdjacents = new HashSet<>(result);
        for (int i = 1; i <= range; i++) {
            final Set<TilePoint> adjacents = getAdjacentTiles(world, origin);
            for (TilePoint adjacent : lastAdjacents) {
                adjacents.addAll(getAdjacentTiles(world, adjacent));
            }

            result.addAll(adjacents);
            lastAdjacents = adjacents;
        }

        return result;
    }
}
