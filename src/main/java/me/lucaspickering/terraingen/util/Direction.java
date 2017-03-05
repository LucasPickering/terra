package me.lucaspickering.terraingen.util;

import me.lucaspickering.terraingen.world.util.TilePoint;

public enum Direction {

    NORTH(0, 1, -1), NORTHEAST(1, 0, -1), SOUTHEAST(1, -1, 0),
    SOUTH(0, -1, 1), SOUTHWEST(-1, 0, 1), NORTHWEST(-1, 1, 0);

    private final TilePoint delta;

    Direction(int x, int y, int z) {
        delta = new TilePoint(x, y, z);
    }

    public TilePoint delta() {
        return delta;
    }

    /**
     * Shifts the given point 1 step in this direction.
     * @see #shift(TilePoint, int)
     */
    public TilePoint shift(TilePoint point) {
        return shift(point, 1);
    }

    /**
     * Shifts the given point the given distance in this direction.
     * @param point the point to shift
     * @param distance the number of steps to shift it (positive)
     * @return the shifted point
     */
    public TilePoint shift(TilePoint point, int distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("Distance must be positive, was: " + distance);
        }
        return point.plus(delta.times(distance));
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
