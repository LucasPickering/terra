package me.lucaspickering.terra.util;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.util.HexPoint;

public enum Direction {

    NORTH(0, 1, -1), NORTHEAST(1, 0, -1), SOUTHEAST(1, -1, 0),
    SOUTH(0, -1, 1), SOUTHWEST(-1, 0, 1), NORTHWEST(-1, 1, 0);

    private final HexPoint delta;

    Direction(int x, int y, int z) {
        delta = new HexPoint(x, y, z);
    }

    public HexPoint delta() {
        return delta;
    }

    /**
     * Shifts the given point 1 step in this direction.
     *
     * @see #shift(HexPoint, int)
     */
    public HexPoint shift(HexPoint point) {
        return shift(point, 1);
    }

    /**
     * Shifts the given point the given distance in this direction.
     *
     * @param point    the point to shift
     * @param distance the number of steps to shift it (non-negative)
     * @return the shifted point
     */
    public HexPoint shift(HexPoint point, int distance) {
        if (distance < 0) {
            throw new IllegalArgumentException(String.format(
                "Distance must be non-negative, was [%d]", distance));
        }
        return point.plus(delta.times(distance));
    }

    public Direction opposite() {
        return values()[(ordinal() + Tile.NUM_SIDES / 2) % Tile.NUM_SIDES];
    }

    public boolean isOpposite(Direction other) {
        // Two directions are opposite each other if each one's x, y, & z is the negation of the
        // other's corresponding coordinate
        return delta().x() == -other.delta().x()
               && delta().y() == -other.delta().y()
               && delta().z() == -other.delta().z();
    }

    public boolean isAdjacentTo(Direction other) {
        // Two directions are adjacent to each other if the distance between their deltas is 1.
        return delta().distanceTo(other.delta()) == 1;
    }
}
