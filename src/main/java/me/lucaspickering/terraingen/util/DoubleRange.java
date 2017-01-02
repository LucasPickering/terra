package me.lucaspickering.terraingen.util;

public class DoubleRange {

    private final double min, max;

    public DoubleRange(double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException(String.format("Min [%f] must be <= max [%f]",
                                                             min, max));
        }
        this.min = min;
        this.max = max;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    /**
     * Is the given value in this range? I.e. is {@code min <= x <= max}.
     *
     * @param x the value to check
     * @return true if the given value is in this range
     */
    public boolean inRange(int x) {
        return min <= x && x <= max;
    }

    /**
     * Coerces the given value into this range.
     *
     * @param x the value to be coerced
     * @return the coerced value
     */
    public double coerce(double x) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}
