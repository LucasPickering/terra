package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.utils.range.Range;


/**
 * Specifies different modes by which tiles can be displayed. Each tile ahs a different input
 * scale and a different output gradient.
 */
public enum TileColorMode {

    ELEVATION(Color.BLACK, Color.WHITE),
    HUMIDITY(Color.WHITE, Color.GREEN),
    BIOME(),
    COMPOSITE();

    private final Color minColor;
    private final Color maxColor;

    TileColorMode() {
        this(null, null);
    }

    TileColorMode(Color minColor, Color maxColor) {
        this.minColor = minColor;
        this.maxColor = maxColor;
    }

    public <T extends Number & Comparable<T>> Color interpolateColor(T value, Range<T> valueRange) {
        final double p = valueRange.normalize(value); // Normalize the value to [0, 1]
        final double q = 1.0 - p; // 1 - the normalize value

        // Interpolate the red, green, and blue values
        final int red = (int) (minColor.getRed() * q + maxColor.getRed() * p);
        final int green = (int) (minColor.getGreen() * q + maxColor.getGreen() * p);
        final int blue = (int) (minColor.getBlue() * q + maxColor.getBlue() * p);

        return new Color(red, green, blue); // Put the RGB values back together
    }
}
