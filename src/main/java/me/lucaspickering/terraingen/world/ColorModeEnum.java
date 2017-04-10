package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * Specifies different modes by which tiles can be displayed. Each tile ahs a different input
 * scale and a different output gradient.
 */
public enum ColorModeEnum {

    ELEVATION(Color.BLACK, Color.WHITE) {
        @Override
        double getNormalizedInput(Tile tile) {
            return World.ELEVATION_RANGE.normalize(tile.elevation());
        }
    },
    HUMIDITY(Color.WHITE, Color.BLUE) {
        @Override
        double getNormalizedInput(Tile tile) {
            return World.ELEVATION_RANGE.normalize(tile.elevation());
        }
    },
    TEMPERATURE(Color.BLUE, Color.RED) {
        @Override
        double getNormalizedInput(Tile tile) {
            return World.ELEVATION_RANGE.normalize(tile.elevation());
        }
    },
    ELEVATION(Color.BLACK, Color.WHITE) {
        @Override
        double getNormalizedInput(Tile tile) {
            return World.ELEVATION_RANGE.normalize(tile.elevation());
        }
    };

    private static final Range<Integer> BYTE_RANGE = new IntRange(0, 255);

    private final Color minColor;
    private final Color maxColor;

    ColorModeEnum(Color minColor, Color maxColor) {
        this.minColor = minColor;
        this.maxColor = maxColor;
    }

    abstract double getNormalizedInput(Tile tile);

    public Color getColor(Tile tile) {
        final double normalInput = getNormalizedInput(tile);

        // Interpolate each component based on the normalized input
        final int red = interpolate(minColor.getRed(), maxColor.getRed(), normalInput);
        final int green = interpolate(minColor.getGreen(), maxColor.getGreen(), normalInput);
        final int blue = interpolate(minColor.getBlue(), maxColor.getBlue(), normalInput);

        return new Color(red, green, blue);
    }

    private int interpolate(int min, int max, double value) {
        return (byte) (min * value + max * (1 - value));
    }
}
