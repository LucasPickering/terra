package me.lucaspickering.terraingen.util;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import me.lucaspickering.terraingen.TerrainGen;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class Funcs {

    private Funcs() {
        // This should never be instantiated
    }

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
        Objects.requireNonNull(random);
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
     * Randomly selects one element from the given non-empty collection. Each element has a
     * chance of being chosen that is proportional to that element's return value from {@code
     * weightFunction}.
     *
     * @param random         the {@link Random} to generate numbers from
     * @param coll           the collection to be chosen from (non-null, non-empty)
     * @param weightFunction the function that provides a weight for each element in the collection
     * @param <T>            the type of the element in the collection
     * @return one randomly-selected element from the given collection
     */
    @NotNull
    public static <T> T randomFromCollectionWeighted(@NotNull Random random,
                                                     @NotNull Collection<T> coll,
                                                     @NotNull Function<T, Integer> weightFunction) {
        Objects.requireNonNull(random);
        Objects.requireNonNull(coll);
        Objects.requireNonNull(weightFunction);
        if (coll.isEmpty()) {
            throw new IllegalArgumentException("Collection cannot be empty");
        }

        // Potential optimization?
        // Create a list with x entires for each element, where x is its weight of being chosen
        final List<T> weightedList = new LinkedList<>();
        for (T value : coll) {
            final int weight = weightFunction.apply(value);
            for (int i = 0; i < weight; i++) {
                weightedList.add(value);
            }
        }

        // Select an element from the weighted list
        return randomFromCollection(random, weightedList);
    }

    public static String getResource(String path, String fileName) {
        return TerrainGen.class.getResource(String.format(path, fileName)).getPath();
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resourcePath the path of the resource to read
     * @param fileName     the file name of the resource
     * @param bufferSize   the initial buffer size
     * @return the resource data
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer ioResourceToByteBuffer(String resourcePath, String fileName,
                                                    int bufferSize) throws IOException {
        ByteBuffer buffer;
        final String resource = String.format(resourcePath, fileName);
        final java.nio.file.Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            try (
                InputStream source = Funcs.class.getClassLoader().getResourceAsStream(resource);
                ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    /**
     * Coerces the given value into the range {@code [min, max]}.
     *
     * @param min the minimum of the range
     * @param x   the value to be coerced
     * @param max the maximum of the range
     * @return the coerced value
     */
    public static double coerce(double min, double x, double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
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
