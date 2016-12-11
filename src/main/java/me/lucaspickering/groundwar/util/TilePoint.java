package me.lucaspickering.groundwar.util;

/**
 * Represents the position of one hex tile. This is meant to be used with the coordinate system
 * described by http://www.redblobgames.com/grids/hexagons/, so {@code x + y + z == 0} must
 * always be {@code true}. Technically this means that keeping z around is redundant, but it
 * makes certain things more elegant (like the distance calculation). Some computations, like
 * {@link #equals} and {@link #hashCode}, don't use z for efficiency purposes.
 */
public class TilePoint {

    private static final String STRING_FORMAT = "(%d, %d, %d)";

    private final int x;
    private final int y;
    private final int z;

    /**
     * Constructs a new TilePoint using the given x and y, inferring the z coordinate (as
     * {@code x + y + z == 0}.
     *
     * @param x the x coord
     * @param y the y coord
     */
    public TilePoint(int x, int y) {
        this(x, y, -x - y); // x + y + z == 0, so z == -x - y
    }

    /**
     * Constructs a new TilePoint using the given coordinates. Verifies that
     * {@code x + y + z == 0} first.
     *
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     */
    public TilePoint(int x, int y, int z) {
        assert x + y + z == 0 : "x + y + z must equal 0";
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    /**
     * Gets the distance between this point and another one.
     *
     * The forumla used is {@code (|x1 - x1| + |y1 - y2| + |z1 - z1|) / 2}.
     *
     * @param other the other point
     * @return the distance between this tile and {@param p}
     */
    public final int distanceTo(TilePoint other) {
        return (Math.abs(x - other.x()) + Math.abs(y - other.y()) + Math.abs(z + other.z())) / 2;
    }

    @Override
    public String toString() {
        return String.format(STRING_FORMAT, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof TilePoint)) {
            return false;
        }

        final TilePoint point = (TilePoint) o;
        return x == point.x && y == point.y; // No need to check z because x + y + z == 0
    }

    @Override
    public int hashCode() {
        // z is a redundant value, so don't include it in the has because it would just reduce
        // the accuracy of the hash.
        return x * 31 + y;
    }
}
