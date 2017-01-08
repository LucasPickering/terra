package me.lucaspickering.terraingen.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.lucaspickering.terraingen.util.Constants;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.Pair;

import static org.lwjgl.system.MemoryStack.stackPush;

public class TrueTypeFont {

    private static final int BITMAP_W = 512, BITMAP_H = 512;

    private final String name;
    private final float fontHeight;
    private final STBTTBakedChar.Buffer charData;

    public TrueTypeFont(String name, float fontHeight) throws IOException, FontFormatException {
        this.name = name;
        this.fontHeight = fontHeight;
        this.charData = init();
    }

    private STBTTBakedChar.Buffer init() {
        final int texID = GL11.glGenTextures();
        final STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96);

        try {
            final ByteBuffer ttf = Funcs.ioResourceToByteBuffer(Constants.FONT_PATH, name,
                                                                160 * 1024);

            final ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            STBTruetype.stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, BITMAP_W, BITMAP_H,
                                             32, cdata);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_ALPHA, BITMAP_W, BITMAP_H, 0,
                              GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        return cdata;
    }

    private float getCharWidth(char c) {
        return fontMetrics.charWidth(c);
    }

    private float getFontHeight() {
        return fontHeight;
    }

    private int getStringWidth(String s) {
        return (int) s.chars()
            .mapToDouble(c -> getCharWidth((char) c)) // Get the width of each character
            .sum(); // Sum up all the widths
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
        final int height = (int) (getFontHeight() * lines.size());

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
        Funcs.setGlColor(color);
        try (MemoryStack stack = stackPush()) {
            final FloatBuffer floatX = stack.floats(x);
            final FloatBuffer floatY = stack.floats(y);
            final STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);

            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    floatX.put(0, 0.0f);
                    floatY.put(0, floatY.get(0) + getFontHeight());
                    continue;
                } else if (c < 32 || 128 <= c) {
                    continue;
                }
                STBTruetype.stbtt_GetBakedQuad(charData, BITMAP_W, BITMAP_H, c - 32,
                                               floatX, floatY, quad, true);

                GL11.glTexCoord2f(quad.s0(), quad.t0());
                GL11.glVertex2f(quad.x0(), quad.y0());

                GL11.glTexCoord2f(quad.s1(), quad.t0());
                GL11.glVertex2f(quad.x1(), quad.y0());

                GL11.glTexCoord2f(quad.s1(), quad.t1());
                GL11.glVertex2f(quad.x1(), quad.y1());

                GL11.glTexCoord2f(quad.s0(), quad.t1());
                GL11.glVertex2f(quad.x0(), quad.y1());
            }
            GL11.glEnd();
        }
    }

    public void delete() {
        charData.free();
    }
}