package me.lucaspickering.groundwar.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Direction;
import me.lucaspickering.groundwar.util.Pair;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public class WorldHelper {

    /**
     * Converts a {@link TilePoint} in the world to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link TilePoint}
     * @return the position of that tile's center on the screen
     */
    public static Point tileToPixel(TilePoint tile) {
        final float x = Tile.WIDTH * tile.x() * 0.75f;
        final float y = -Tile.HEIGHT * (tile.x() / 2.0f + tile.y());
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
    public static TilePoint pixelToTile(Point pos) {
        // Shift the point so that the origin is the middle of the screen
        final Point shifted = pos.minus(Constants.WORLD_CENTER);

        // Convert it to a fractional tile point
        final float fracX = shifted.x() * 2f / 3f / Tile.RADIUS;
        final float fracY = -(shifted.x() + (float) Math.sqrt(3) * shifted.y())
                            / (Tile.RADIUS * 3f);
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
     * @param world  the set of tiles in the world
     * @param origin the center of the search
     * @return tiles adjacent to {@code origin}, in a direction:point map
     * @throws IllegalArgumentException if {@code origin} is not in {@code world}
     */
    public static Map<Direction, TilePoint> getAdjacentTiles(Set<TilePoint> world,
                                                             TilePoint origin) {
        Objects.requireNonNull(origin);
        if (!world.contains(origin)) {
            throw new IllegalArgumentException("Origin is not in the world");
        }

        // Collect all tiles that are adjacent to origin and also in the world
        return Arrays.stream(Direction.values())
            .map(dir -> new Pair<>(dir, origin.plus(dir.delta()))) // Get the point in each dir
            .filter(pair -> world.contains(pair.second())) // Only take tiles that are in the world
            .collect(Pair.mapCollector()); // Collect into a map
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
            // Start with tiles directly adjacent to this one
            final Set<TilePoint> adjacents =
                new HashSet<>(getAdjacentTiles(world, origin).values());
            for (TilePoint adjacent : lastAdjacents) {
                adjacents.addAll(getAdjacentTiles(world, adjacent).values());
            }

            result.addAll(adjacents);
            lastAdjacents = adjacents;
        }

        return result;
    }
}
