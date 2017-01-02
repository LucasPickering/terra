package me.lucaspickering.terraingen.util;

/**
 * Represents the position of one hex tile. This is meant to be used with the coordinate system
 * described by http://www.redblobgames.com/grids/hexagons/, so {@code x + y + z == 0} must
 * always be {@code true}. Technically this means that keeping z around is redundant, but it
 * makes certain things more elegant (like the distance calculation). Some computations, like
 * {@link #equals} and {@link #hashCode}, don't use z for efficiency purposes.
 */
public class TilePoint {

    public static final TilePoint ZERO = new TilePoint(0, 0, 0);

    private static final String STRING_FORMAT = "(%d, %d, %d)";

    private final int x, y, z;

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
     * @throws IllegalArgumentException if {@code x + y + z != 0}
     */
    public TilePoint(int x, int y, int z) {
        if (x + y + z != 0) {
            throw new IllegalArgumentException("x + y + z must equal 0");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Rounds the given fractional coordinate values into valid integer coordinates, and creates
     * a {@link TilePoint} from those. The round point closest to the given fractional point will
     * be returned.
     *
     * @param x the fractional x
     * @param y the fractional y
     * @param z the fractional z
     * @return the rounded point
     */
    public static TilePoint roundPoint(float x, float y, float z) {
        // Convert the fractional tile coordinates to regular coordinates
        // First, get rounded versions of each coord
        int roundX = Math.round(x);
        int roundY = Math.round(y);
        int roundZ = Math.round(z);

        // roundX + roundY + roundZ == 0 is not guaranteed, so we need to recalculate one of them

        // Find how much each one needed to be rounded
        final float xDiff = Math.abs(x - roundX);
        final float yDiff = Math.abs(y - roundY);
        final float zDiff = Math.abs(z - roundZ);

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
     * The formula used is {@code (|x1 - x2| + |y1 - y2| + |z1 - z2|) / 2}.
     *
     * @param other the other point
     * @return the distance between this tile and {@param p}
     */
    public final int distanceTo(TilePoint other) {
        return (Math.abs(x - other.x())
                + Math.abs(y - other.y())
                + Math.abs(z - other.z())) / 2;
    }

    public final TilePoint plus(TilePoint other) {
        return new TilePoint(x + other.x(), y + other.y(), z + other.z());
    }

    public final TilePoint times(int factor) {
        return new TilePoint(x * factor, y * factor, z * factor);
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
        // z is redundant, so don't include it in the hash because it would just reduce accuracy
        return x * 31 + y;
    }
}
