package me.lucaspickering.terra.world.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the position of one hex tile. This is meant to be used with the coordinate system
 * described by http://www.redblobgames.com/grids/hexagons/, so {@code x + y + z == 0}.
 * Technically this means that keeping z around is redundant, but it makes certain things more
 * elegant (like the distance calculation). Some computations, like
 * {@link #equals} and {@link #hashCode}, don't use z for efficiency purposes.
 */
public class HexPoint implements Comparable<HexPoint> {

    public static final HexPoint ZERO = new HexPoint(0, 0, 0);

    private static final String STRING_FORMAT = "(%d, %d, %d)";

    private final int x, y, z;

    /**
     * Constructs a new HexPoint using the given x and y coordinates. The z coordinate is
     * calculated from the x and y.
     *
     * @param x the x coord
     * @param y the y coord
     */
    public HexPoint(int x, int y) {
        this(x, y, -x - y);
    }

    /**
     * Constructs a new HexPoint using the given coordinates. Verifies that
     * {@code x + y + z == 0} first.
     *
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     * @throws IllegalArgumentException if {@code x + y + z != 0}
     */
    public HexPoint(int x, int y, int z) {
        if (x + y + z != 0) {
            throw new IllegalArgumentException("x + y + z must equal 0");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Rounds the given fractional coordinate values into valid integer coordinates, and creates
     * a {@link HexPoint} from those. The round point closest to the given fractional point will
     * be returned.
     *
     * @param x the fractional x
     * @param y the fractional y
     * @param z the fractional z
     * @return the rounded point
     */
    public static HexPoint roundPoint(double x, double y, double z) {
        // Convert the fractional tile coordinates to regular coordinates
        // First, get rounded versions of each coord
        int roundX = (int) Math.round(x);
        int roundY = (int) Math.round(y);
        int roundZ = (int) Math.round(z);

        // roundX + roundY + roundZ == 0 is not guaranteed, so we need to recalculate one of them

        // Find how much each one needed to be rounded
        final double xDiff = Math.abs(x - roundX);
        final double yDiff = Math.abs(y - roundY);
        final double zDiff = Math.abs(z - roundZ);

        // Recalculate the one that rounded the most
        if (xDiff > yDiff && xDiff > zDiff) {
            roundX = -roundY - roundZ;
        } else if (yDiff > zDiff) {
            roundY = -roundX - roundZ;
        } else {
            roundZ = -roundX - roundY;
        }

        return new HexPoint(roundX, roundY, roundZ);
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
    public final int distanceTo(HexPoint other) {
        return (Math.abs(x - other.x())
                + Math.abs(y - other.y())
                + Math.abs(z - other.z())) / 2;
    }

    public final HexPoint plus(int x, int y, int z) {
        return new HexPoint(this.x + x, this.y + y, this.z + z);
    }

    public final HexPoint plus(HexPoint other) {
        return plus(other.x(), other.y(), other.z());
    }

    public final HexPoint times(int factor) {
        return new HexPoint(x * factor, y * factor, z * factor);
    }

    @Override
    public String toString() {
        return String.format(STRING_FORMAT, x, y, z);
    }

    public boolean equals(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof HexPoint)) {
            return false;
        }

        final HexPoint point = (HexPoint) o;
        return equals(point.x, point.y, point.z);
    }

    @Override
    public int hashCode() {
        // z is redundant, so don't include it in the hash because it would just reduce accuracy
        return x * 31 + y;
    }

    @Override
    public int compareTo(@NotNull HexPoint other) {
        // Compares by x then y
        int comp;

        // Compare x, fall back to y if x's are equal
        comp = Integer.compare(x, other.x());
        if (comp != 0) {
            return comp;
        }

        // Compare y and return that result
        comp = Integer.compare(y, other.y());
        return comp;

        // No need to compare z. If x and y are equal, z must be equal, by this class's contract
        // of x + y + z == 0.
    }
}
