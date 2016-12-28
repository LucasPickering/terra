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

        // Select a random element from the collection
        // Exception will be thrown if the collection is empty
        return coll.stream()
            .skip(random.nextInt(coll.size()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Collection cannot be empty"));
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
