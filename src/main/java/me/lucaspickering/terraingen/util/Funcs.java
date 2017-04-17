package me.lucaspickering.terraingen.util;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.utils.MathFuncs;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;
import static org.lwjgl.BufferUtils.createByteBuffer;

public class Funcs {

    private static final Range<Integer> BYTE_RANGE = new IntRange(0, 255);

    private Funcs() {
        // This should never be instantiated
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

    public static void setGlColor(Color color) {
        GL11.glColor4f(color.getRed() / 255f,
                       color.getGreen() / 255f,
                       color.getBlue() / 255f,
                       color.getAlpha() / 255f);
    }

    /**
     * Gets the brightness value of the given color.
     *
     * @param color the color
     * @return the brightness of color, i.e. the third component of its HSB form
     */
    public static float getColorBrightness(@NotNull Color color) {
        return MathFuncs.max(color.getRed(), color.getGreen(), color.getBlue()) / 255f;
    }

    /**
     * Blends the two given colors according to their individual alpha values. A color with a
     * higher alpha will have a stronger effect on the blended color.
     *
     * @param c1 the first color
     * @param c2 the second color
     * @return the blended color
     */
    @NotNull
    public static Color blendColors(Color c1, Color c2) {
        final float alpha1 = c1.getAlpha() / 255f;
        final float alpha2 = c2.getAlpha() / 255f;

        final int red = Math.min((int) (c1.getRed() * alpha1 + c2.getRed() * alpha2), 255);
        final int green = Math.min((int) (c1.getGreen() * alpha1 + c2.getGreen() * alpha2), 255);
        final int blue = Math.min((int) (c1.getBlue() * alpha1 + c2.getBlue() * alpha2), 255);
        final int alpha = Math.min((int) ((alpha1 + alpha2) * 255), 255);
        return new Color(red, green, blue, alpha);
    }


    /**
     * Overlays the first color onto the second. If the foreground color is opaque, then the
     * result will be just the foreground. If the foreground is transparent, then the result will
     * be just the background. Otherwise, foreground is blended into the background according to
     * its alpha value.
     *
     * @param fg the foreground color
     * @param bg the background color
     * @return the blended color
     */
    @NotNull
    public static Color overlayColors(Color fg, Color bg) {
        final float fgAlpha = fg.getAlpha() / 255f;
        final float invFgAlpha = 1f - fgAlpha;

        int red = (int) (fg.getRed() * fgAlpha + bg.getRed() * invFgAlpha);
        int green = (int) (fg.getGreen() * fgAlpha + bg.getGreen() * invFgAlpha);
        int blue = (int) (fg.getBlue() * fgAlpha + bg.getBlue() * invFgAlpha);
        int alpha = (int) (fgAlpha * 255) + bg.getAlpha();

        red = BYTE_RANGE.coerce(red);
        green = BYTE_RANGE.coerce(green);
        blue = BYTE_RANGE.coerce(blue);
        alpha = BYTE_RANGE.coerce(alpha);

        return new Color(red, green, blue, alpha);
    }
}
