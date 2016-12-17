package me.lucaspickering.groundwar.util;

import java.util.Objects;

import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;

/**
 * A class representing an immutable 2-dimensional integer point. Used mostly for points on-screen.
 */
public class Point implements Cloneable {

    /**
     * {@link Point}s are immutable, so this globally-available zero point can save some time &
     * space. This can be used as a placeholder {@link Point} without having to instantiate a new
     * object.
     */
    public static final Point ZERO = new Point(0, 0);

    private final int x, y;

    /**
     * Constructs a new {@code Point} with the given x and y.
     *
     * @param x the x-value
     * @param y the y-value
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * Creates a new point whose coordinates are the sum of this point's and the given point's. In
     * other words, this creates a {@code new Point(this.x + p.x, this.y + p.y)}.
     *
     * @param p the point to be added with this one
     * @return the new {@code Point}
     */
    public Point plus(Point p) {
        return plus(p.x(), p.y());
    }

    /**
     * Creates a new point whose coordinates are the sum of this point's and x and y. In other
     * words, this creates a {@code new Point(this.x + x, this.y + y)}.
     *
     * @param x the x to be added
     * @param y the y to be added
     * @return the new {@code Point}
     */
    public Point plus(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point minus(Point p) {
        return minus(p.x(), p.y());
    }

    public Point minus(int x, int y) {
        return new Point(this.x - x, this.y - y);
    }

    /**
     * Creates a new point, adjusting this one based on the given alignments and dimensions.
     *
     * The returned value will be this point, but adjusted to be the top-left corner of the
     * element with the given properties.
     *
     * @param horizAlign the horizontal alignment
     * @param vertAlign  the vertical alignment
     * @param width      the height
     * @param height     the height
     * @return a new point that has been adjusted to be the top-left corner of an element
     */
    public Point adjustForAlignment(HorizAlignment horizAlign, VertAlignment vertAlign,
                                    int width, int height) {
        return plus(horizAlign.leftAdjustment(width), vertAlign.topAdjustment(height));
    }

    /**
     * Gets the Euclidean distance between this point and another point.
     *
     * @param p the other point (non-null)
     * @return the Euclidean distance between the two points
     * @throws NullPointerException if {@code p == null}
     */
    public double distanceTo(Point p) {
        Objects.requireNonNull(p);
        final int xDiff = x - p.x;
        final int yDiff = y - p.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Point)) {
            return false;
        }

        final Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
