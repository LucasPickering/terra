package me.lucaspickering.terraingen.world;

import java.awt.Color;
import java.util.function.Function;

import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * Specifies different modes by which tiles can be displayed. Each tile ahs a different input
 * scale and a different output gradient.
 */
public class ColorMode<T extends Number & Comparable<T>> {

    private static final Range<Integer> BYTE_RANGE = new IntRange(0, 255);

    public static final ColorMode<Integer> ELEVATION = new ColorMode<>(Tile::elevation,
                                                                       World.ELEVATION_RANGE,
                                                                       Color.BLACK, Color.WHITE);
    public static final ColorMode<Double> HUMIDITY = new ColorMode<>(Tile::humidity,
                                                                     World.HUMIDITY_RANGE,
                                                                     Color.WHITE, Color.GREEN);

    private final Function<Tile, T> inputFunc;
    private final Range<T> inputRange;
    private final Color minColor;
    private final Color maxColor;

    ColorMode(Function<Tile, T> inputFunc, Range<T> inputRange, Color minColor, Color maxColor) {
        this.inputFunc = inputFunc;
        this.inputRange = inputRange;
        this.minColor = minColor;
        this.maxColor = maxColor;
    }

    public Color getColor(Tile tile) {
        final T input = inputFunc.apply(tile);
        final double normalInput = inputRange.normalize(input);

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
