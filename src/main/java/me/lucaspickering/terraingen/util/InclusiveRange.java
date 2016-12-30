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

    public int randomIn(Random random) {
        return min + random.nextInt(size());
    }
}
