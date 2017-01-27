package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.terraingen.world.util.Cluster;

public class Continent {

    private final Cluster tiles;

    private Color debugColor;

    public Continent(Cluster tiles) {
        this.tiles = tiles;
    }

    public Continent(Continent continent) {
        this(continent.tiles);
        this.debugColor = continent.getDebugColor();
    }

    public Cluster getTiles() {
        return tiles;
    }

    /**
     * Generates a color to represent this continent.
     *
     * @return a semi-unique color to represent this continent
     */
    private Color computeColor() {
        // Continent sizes are generally 200-1000, or 8-10 bits. Round that up to 12 bits.
        final int size = tiles.size(); // Consider this a 12-bit number

        // Isolate each 4-bit section of the 12-bit number
        final int lower = size & 0x00f; // Lower 4 bits
        final int middle = size & 0x0f0; // Middle 4 bits
        final int upper = size & 0xf00; // upper 4 bits

        final int red = middle | lower; // Lower 8 bits
        final int green = (upper >> 4) | lower; // Upper 4 bits and lower 4 bits
        final int blue = (upper | middle) >> 4; // Upper 8 bits
        return new Color(red, green, blue, 200);
    }

    /**
     * Gets the debug color of this continent. If the color has not yet been computed, that will
     * be done now, otherwise the cached color will be used. The computed color will be
     * semi-unique, dependent on the properties of this continent.
     *
     * @return the debug color
     */
    public Color getDebugColor() {
        if (debugColor == null) {
            debugColor = computeColor();
        }
        return debugColor;
    }
}
