package me.lucaspickering.terra.util;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.utils.MathFuncs;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

public class Funcs {

    private static final Range<Integer> BYTE_RANGE = new IntRange(0, 255);

    private Funcs() {
        // This should never be instantiated
    }

    /**
     * Gets the brightness value of the given color.
     *
     * @param color the color
     * @return the brightness of color, i.e. the third component of its HSB form
     */
    public static float getColorBrightness(@NotNull Color color) {
        return MathFuncs.max(color.r, color.g, color.b);
    }

    /**
     * Run the given procedure and time how long it takes.
     *
     * @param runnable the procedure to run
     * @return the time the procedure took to execute, in milliseconds
     */
    public static long timed(Runnable runnable) {
        final long startTime = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - startTime;
    }
}
