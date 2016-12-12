package me.lucaspickering.groundwar.util;

import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;

import me.lucaspickering.groundwar.world.tile.Tile;

public class Funcs {

    /**
     * Randomly selects one element from the given non-empty collection. Each element has an equal
     * chance of being chosen.
     *
     * @param random the {@link Random} to generate numbers from
     * @param coll   the collection to be chosen from (non-null, non-empty)
     * @param <T>    the type of the element in the collection
     * @return one randomly-selected, even-distributed element from the given collection
     */
    public static <T> T randomFromCollection(Random random, Collection<T> coll) {
        assert coll != null && !coll.isEmpty() : "Collection cannot be null or empty";

        // Select a random element from the collection. The error should never be thrown.
        return coll.stream().skip(random.nextInt(coll.size())).findFirst().orElseThrow(
            () -> new AssertionError("No random element selected despite non-empty collection"));
    }

    public static TilePoint tilePosFromScreenPos(Point pos) {
        final Point shiftedPos = pos.minus(Constants.WORLD_CENTER);
        final int x = (int) (shiftedPos.x() / (Tile.TILE_WIDTH * 0.75f));
        final int y = -x / 2 - shiftedPos.y() / Tile.TILE_HEIGHT;
        return new TilePoint(x, y);
    }

    /**
     * Make an ARGB color where {@code A = 255}, and {@code R = G = B = input}. If the input is not in
     * the range [0, 255], it will be masked to fall into that range.
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
