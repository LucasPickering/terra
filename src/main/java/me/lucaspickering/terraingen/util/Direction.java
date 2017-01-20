package me.lucaspickering.terraingen.util;

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

    public TilePoint shift(TilePoint point) {
        return point.plus(delta);
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
