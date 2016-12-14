package me.lucaspickering.groundwar.util;

import java.util.Random;

public class InclusiveRange {

    private final int min, max;

    public InclusiveRange(int min, int max) {
        assert min <= max : "min must be <= max";
        this.min = min;
        this.max = max;
    }

    public int size() {
        return max - min + 1;
    }

    public boolean inRange(int n) {
        // n is in this range if min <= n <= max
        return min <= n && n <= max;
    }

    public int randomIn(Random random) {
        return min + random.nextInt(size());
    }
}
