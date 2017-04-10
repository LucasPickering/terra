package me.lucaspickering.terraingen.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.awt.Color;
import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.lucaspickering.terraingen.util.Constants;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.utils.Pair;
import static org.lwjgl.system.MemoryStack.stackPush;

public class TrueTypeFont {

    private static final int CHAR_DATA_SIZE = 96; // Size of the character data buffer, in bytes
    private static final int BITMAP_SIZE = 1024;
    private static final int TTF_BUFFER_SIZE = 160 * 1024; // Size of the buffer to load TTF into
    private static final int FIRST_CHAR = 32;

    private final Font font;
    private final int textureID;
    private final STBTTBakedChar.Buffer charData;

    public TrueTypeFont(Font font) throws IOException, FontFormatException {
        this.font = font;
        this.textureID = GL11.glGenTextures(); // Create a new texture ID for the font map
        this.charData = initCharBuffer();
    }

    private STBTTBakedChar.Buffer initCharBuffer() {
        final STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(CHAR_DATA_SIZE);

        try {
            final ByteBuffer ttf = Funcs.ioResourceToByteBuffer(Constants.FONT_PATH,
                                                                font.getFontName(),
                                                                TTF_BUFFER_SIZE);

            final ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_SIZE * BITMAP_SIZE);
            STBTruetype.stbtt_BakeFontBitmap(ttf, getFontHeight(), bitmap, BITMAP_SIZE, BITMAP_SIZE,
                                             FIRST_CHAR, cdata);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_ALPHA, BITMAP_SIZE, BITMAP_SIZE, 0,
                              GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return cdata;
    }

    private int getFontHeight() {
        return font.getFontHeight();
    }

    private int getStringWidth(String s) {
        // Width of the string is the sum of all the character widths
        try (MemoryStack stack = stackPush()) {
            final FloatBuffer xFloatBuffer = stack.floats(0f);
            final FloatBuffer yFloatBuffer = stack.floats(0f);
            final STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
            for (char c : s.toCharArray()) {
                STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_SIZE, BITMAP_SIZE,
                                               c - FIRST_CHAR, xFloatBuffer, yFloatBuffer,
                                               quad, true);
            }
            return (int) xFloatBuffer.get(0);
        }
    }

    /**
     * @see #getStringSize(String)
     */
    private Pair<Integer, Integer> getStringSize(List<String> lines) {
        // Calculate width as the max of the width of each line
        final int width = lines.stream()
            .mapToInt(this::getStringWidth) // Get the width of each string
            .max() // Find max of each width
            .orElse(0); // Get the max or 0 if there is none

        // Calculate height as the sum of the height of each line
        final int height = getFontHeight() * lines.size();

        return new Pair<>(width, height);
    }

    /**
     * Gets the size of the given string in pixels when drawn. The size is given as a
     * (width, height) pair.
     *
     * @param text the string to be drawn
     * @return the size of the string in (width, height) format
     */
    public Pair<Integer, Integer> getStringSize(String text) {
        Objects.requireNonNull(text);
        return getStringSize(splitLines(text));
    }

    private List<String> splitLines(String text) {
        return Arrays.asList(text.split("\n"));
    }

    private boolean isDrawable(char c) {
        // Range of drawable ASCII characters
        return FIRST_CHAR <= c && c <= 127;
    }

    private float shiftY(int y) {
        // Magic method for correcting y position of text. Not sure why this works.
        return y + getFontHeight() * 0.5f + 4f;
    }

    /**
     * Draw the given text in this font.
     *
     * @param text       the text to draw (non-null)
     * @param x          the x location to draw at
     * @param y          the y location to draw at
     * @param color      the color to draw with
     * @param horizAlign the {@link HorizAlignment} to draw with
     * @param vertAlign  the {@link VertAlignment} to draw with
     * @throws NullPointerException if {@code text == null}
     */
    public void draw(String text, int x, int y, Color color,
                     HorizAlignment horizAlign, VertAlignment vertAlign) {
        Objects.requireNonNull(text);

        final List<String> lines = splitLines(text);
        final Pair<Integer, Integer> stringSize = getStringSize(lines); // Pair of (width, height)
        x += horizAlign.leftAdjustment(stringSize.first()); // Shift x for alignment
        y += vertAlign.topAdjustment(stringSize.second()); // Shift y for alignment

        // Bind this font's texture and set the color
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        Funcs.setGlColor(color);

        // Draw the text
        try (MemoryStack stack = stackPush()) {
            final FloatBuffer xFloatBuffer = stack.floats(x);
            final FloatBuffer yFloatBuffer = stack.floats(shiftY(y));
            final STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
            GL11.glBegin(GL11.GL_QUADS);

            // Draw each line
            for (String line : lines) {
                // Draw each character in the line
                for (char c : line.toCharArray()) {
                    // Skip invalid characters
                    if (!isDrawable(c)) {
                        continue;
                    }

                    STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_SIZE, BITMAP_SIZE,
                                                   c - FIRST_CHAR, xFloatBuffer, yFloatBuffer,
                                                   quad, true);

                    GL11.glTexCoord2f(quad.s0(), quad.t0());
                    GL11.glVertex2f(quad.x0(), quad.y0());

                    GL11.glTexCoord2f(quad.s1(), quad.t0());
                    GL11.glVertex2f(quad.x1(), quad.y0());

                    GL11.glTexCoord2f(quad.s1(), quad.t1());
                    GL11.glVertex2f(quad.x1(), quad.y1());

                    GL11.glTexCoord2f(quad.s0(), quad.t1());
                    GL11.glVertex2f(quad.x0(), quad.y1());
                }

                // Keep the same x, increase y by the height of the font
                xFloatBuffer.put(0, x);
                yFloatBuffer.put(0, yFloatBuffer.get(0) + getFontHeight());
            }
            GL11.glEnd();
        }
    }

    public void delete() {
        charData.free();
    }
}