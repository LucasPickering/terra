package me.lucaspickering.terraingen.util;

import java.util.Random;

public class InclusiveRange {

    private final int min, max;

    public InclusiveRange(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException(String.format("Min [%d] must be <= max [%d]",
                                                             min, max));
        }
        this.min = min;
        this.max = max;
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

    /**
     * Coerces the given value into this range. If {@code x} is already in this range, the same
     * value is returned. If {@code x < min}, {@code min} is returned. If {@code x > max}, {@code
     * max} is returned.
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

    /**
     * Normalizes to the range {@code [0, 1]}
     *
     * @see #normalize(int, float, float)
     */
    public float normalize(int x) {
        return normalize(x, 0, 1);
    }

    /**
     * Normalizes the given value within this range to the specified range {@code [newMin, newMax]}.
     * If the given value isn't already in this range, it will be coerced, then mapped. If the
     * given max is less than the given min, the value will be mapped to {@code [newMax, newMin]}
     * but be inverted.
     *
     * @param x the value to normalize
     * @return {@code x} mapped to the range {@code [0, 1]}
     */
    public float normalize(int x, float newMin, float newMax) {
        x = coerce(x); // Coerce x first

        // If max < min, flip those values, but we'll need to invert the mapped value later
        final boolean invert = newMax < newMin;
        if (invert) {
            final float temp = newMin;
            newMin = newMax;
            newMax = temp;
        }

        // Then map to [0, 1]
        float halfMapped = (float) (x - min) / (max - min);

        // Invert if needed
        if (invert) {
            halfMapped = 1f - halfMapped;
        }

        // Now map to [newMin, newMax]
        return halfMapped * (newMax - newMin) + newMin;
    }

    public int randomIn(Random random) {
        return min + random.nextInt(size());
    }
}
