package me.lucaspickering.groundwar.util;

import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Random;

public class Funcs {

    /**
     * Generates a random number in the range [min, max].
     *
     * @param random the random generator to be used
     * @param min    the minimum of the number
     * @param max    the maximum of the number
     */
    public static float randomInRange(Random random, float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    /**
     * Generates a random number in the range [min, max].
     *
     * @param random the random generator to be used
     * @param min    the minimum of the number
     * @param max    the maximum of the number
     */
    public static int randomInRange(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Make an ARGB color where {@code A = 255}, and {@code R = G = B = input}. If the input is
     * not in the range [0, 255], it will be masked to fall into that range.
     *
     * @return the color hex code
     */
    public static int makeGray(int input) {
        input &= 0xff; // Mask the input to be in the range [0, 255]
        final int a = 0xff000000;
        final int r = input << 8;
        final int g = input << 4;
        final int b = input;
        return a | r | g | b;
    }

    public static void setGlColor(Color color) {
        GL11.glColor4f(color.getRed() / 255f,
                       color.getGreen() / 255f,
                       color.getBlue() / 255f,
                       color.getAlpha() / 255f);
    }

    public static Color colorFromArgb(int argb) {
        final int alpha = argb >> 24 & 0xff;
        final int red = argb >> 16 & 0xff;
        final int green = argb >> 8 & 0xff;
        final int blue = argb & 0xff;
        return new Color(red, green, blue, alpha);
    }
}
