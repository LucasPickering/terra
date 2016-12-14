package me.lucaspickering.groundwar.util;

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
}
