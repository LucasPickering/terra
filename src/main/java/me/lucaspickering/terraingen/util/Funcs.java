package me.lucaspickering.terraingen.util;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class Funcs {

    /**
     * Returns the first element in a {@link Collection}. Technically collections don't have
     * ordering so this is effectively the same as {@link #randomFromCollection}, but a little
     * bit faster.
     *
     * @param coll the collection to be chosen from (non-null, non-empty)
     * @param <T>  the type of the element in the collection
     * @return the first element in the collection, as determined by its iterator
     */
    @NotNull
    public static <T> T firstFromCollection(@NotNull Collection<T> coll) {
        Objects.requireNonNull(coll);

        // Exception will be thrown if the collection is empty
        return coll.stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Collection cannot be empty"));
    }

    /**
     * Randomly selects one element from the given non-empty collection. Each element has an equal
     * chance of being chosen.
     *
     * @param random the {@link Random} to generate numbers from
     * @param coll   the collection to be chosen from (non-null, non-empty)
     * @param <T>    the type of the element in the collection
     * @return one randomly-selected, even-distributed element from the given collection
     */
    @NotNull
    public static <T> T randomFromCollection(@NotNull Random random, @NotNull Collection<T> coll) {
        Objects.requireNonNull(coll);
        if (coll.isEmpty()) {
            throw new IllegalArgumentException("Collection cannot be empty");
        }

        // Select a random element from the collection, exception can never be thrown
        return coll.stream()
            .skip(random.nextInt(coll.size()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Can't get here"));
    }

    /**
     * Coerces the given value into the range {@code [min, max]}.
     *
     * @param min the minimum of the range
     * @param x   the value to be coerced
     * @param max the maximum of the range
     * @return the coerced value
     */
    public static float coerce(float min, float x, float max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }

    /**
     * Maps the given value from one given range to the other given range. For example, 5 mapped
     * from [0, 10] to [20, 40] is 30.
     *
     * @param fromMin the minimum of the range being mapped from
     * @param fromMax the maximum of the range being mapped from
     * @param toMin   the minimum of the range being mapped to
     * @param toMax   the maximum of the range being mapped to
     * @param x       the value being mapped
     * @return the mapped value
     */
    public static float mapToRange(float fromMin, float fromMax,
                                   float toMin, float toMax, float x) {
        float halfMapped = (x - fromMin) / (fromMax - fromMin); // Map to [0, 1]
        return halfMapped * (toMax - toMin) + toMin; // Now map to [toMin, toMax]
    }

    /**
     * Applies a random amount of slop to the given value.
     *
     * @param random  the {@link Random} to use
     * @param x       the value to be randomized
     * @param maxSlop the maximum amount of slop to apply
     * @return a random, uniformly distributed value in the range {@code [x - maxSlop, x + maxSlop]}
     */
    public static int randomSlop(Random random, int x, int maxSlop) {
        return x + random.nextInt(maxSlop * 2 + 1) - maxSlop;
    }

    public static void setGlColor(Color color) {
        GL11.glColor4f(color.getRed() / 255f,
                       color.getGreen() / 255f,
                       color.getBlue() / 255f,
                       color.getAlpha() / 255f);
    }

    /**
     * Creates a {@link Color} from the given RGB code with alpha 255 (opaque).
     *
     * @param rgb the RGB code
     * @return the given color as a {@link Color} with alpha 255
     */
    @NotNull
    public static Color colorFromRgb(int rgb) {
        return new Color(rgb); // Set alpha to 255
    }

    /**
     * Creates a {@link Color} from the given ARGB (alpha-red-green-blue) code.
     *
     * @param argb the ARGB code
     * @return the given color as a {@link Color}
     */
    @NotNull
    public static Color colorFromArgb(int argb) {
        final int alpha = argb >> 24 & 0xff;
        final int red = argb >> 16 & 0xff;
        final int green = argb >> 8 & 0xff;
        final int blue = argb & 0xff;
        return new Color(red, green, blue, alpha);
    }

    /**
     * Converts the given color to a Hue-Saturation-Value array.
     *
     * @param color the color in RGB form
     * @return the color as a float array of Hue-Saturation-Value (in that order)
     */
    @NotNull
    public static float[] toHSV(@NotNull Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    @NotNull
    public static Color toRGB(@NotNull float[] hsv) {
        return toRGB(hsv[0], hsv[1], hsv[2]);
    }

    @NotNull
    public static Color toRGB(float hue, float saturation, float value) {
        return colorFromRgb(Color.HSBtoRGB(hue, saturation, value));
    }
}
