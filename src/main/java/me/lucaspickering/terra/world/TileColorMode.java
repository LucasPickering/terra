package me.lucaspickering.terra.world;

import java.awt.Color;

import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;


/**
 * Specifies different modes by which tiles can be displayed. Each tile ahs a different input
 * scale and a different output gradient.
 */
public enum TileColorMode {

    ELEVATION {
        @Override
        public Color getColor(Tile tile) {
            return interpolateColor(Color.BLACK, Color.WHITE,
                                    tile.elevation(), World.ELEVATION_RANGE);
        }
    },
    HUMIDITY {
        @Override
        public Color getColor(Tile tile) {
            // Water tiles are always blue
            if (tile.biome().isWater()) {
                return Color.BLUE;
            }
            return interpolateColor(Color.WHITE, Color.GREEN,
                                    tile.humidity(), World.HUMIDITY_RANGE);
        }
    },
    WATER_LEVEL {
        @Override
        public Color getColor(Tile tile) {
            // Water tiles are always black
            if (tile.biome().isWater()) {
                return Color.BLACK;
            }
            return interpolateColor(Color.WHITE, Color.BLUE,
                                    tile.getWaterLevel(), WATER_LEVEL_RANGE);
        }
    },
    WATER_TRAVERSED {
        @Override
        public Color getColor(Tile tile) {
            // Water tiles are always black
            if (tile.biome().isWater()) {
                return Color.BLACK;
            }
            return interpolateColor(Color.WHITE, Color.BLUE,
                                    tile.getWaterTraversed(), WATER_TRAVERSED_RANGE);
        }
    },
    BIOME {
        @Override
        public Color getColor(Tile tile) {
            return tile.biome().color();
        }
    },
    COMPOSITE {
        @Override
        public Color getColor(Tile tile) {
            // Combine elevation and biome color
            final Color elevColor = ELEVATION.getColor(tile);
            float elevBrightness = Funcs.getColorBrightness(elevColor);
            elevBrightness = (float) Math.pow(elevBrightness, 0.75); // Make it brighter

            // Scale this biome color's brightness by the brightness of the elevation color
            final Color biomeColor = tile.biome().color();
            return new Color((int) (biomeColor.getRed() * elevBrightness),
                             (int) (biomeColor.getGreen() * elevBrightness),
                             (int) (biomeColor.getBlue() * elevBrightness));
        }
    };

    // Used for shading
    private static final Range<Double> WATER_LEVEL_RANGE = new DoubleRange(0.0, 10.0);
    private static final Range<Double> WATER_TRAVERSED_RANGE = new DoubleRange(0.0, 20.0);

    public abstract Color getColor(Tile tile);

    private static <T extends Number & Comparable<T>>
    Color interpolateColor(Color minColor, Color maxColor, T value, Range<T> valueRange) {
        final double p = valueRange.normalize(value); // Normalize the value to [0, 1]
        final double q = 1.0 - p; // 1 - the normalize value

        // Interpolate the red, green, and blue values
        final int red = (int) (minColor.getRed() * q + maxColor.getRed() * p);
        final int green = (int) (minColor.getGreen() * q + maxColor.getGreen() * p);
        final int blue = (int) (minColor.getBlue() * q + maxColor.getBlue() * p);

        return new Color(red, green, blue); // Put the RGB values back together
    }
}
