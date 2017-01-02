package me.lucaspickering.terraingen.util;

import java.util.Random;

public class IntRange {

    private final int min, max;

    public IntRange(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException(String.format("Min [%d] must be <= max [%d]",
                                                             min, max));
        }
        this.min = min;
        this.max = max;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public int size() {
        return max - min + 1;
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

    public int randomIn(Random random) {
        return min + random.nextInt(size());
    }

    /**
     * Coerces the given value into this range.
     *
     * @param x the value to be coerced
     * @return the coerced value
     */
    public int coerce(int x) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}
