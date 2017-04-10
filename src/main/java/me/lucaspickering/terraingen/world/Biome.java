package me.lucaspickering.terraingen.world;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

public enum Biome {

    // You can adjust how much the value of the tile color changes in relation to the elevation.
    // For more value change, use a larger range. You can have the range extend outside [0, 1],
    // and it will be coerced if necessary.
    OCEAN("Ocean", 0x1653b7, new DoubleRange(-25.0, -10.0), new DoubleRange(0.4, 0.7)),
    COAST("Coast", 0x1887b2, new DoubleRange(-15.0, 0.0), new DoubleRange(0.5, 1.0)),
    LAKE("Lake", 0x09729b, new DoubleRange(-10.0, 0.0), new DoubleRange(0.5, 1.0)),
    BEACH("Beach", 0xf2ef59, new DoubleRange(0.0, 5.0), new DoubleRange(0.75, 0.95)),
    PLAINS("Plains", 0xb9f442, new DoubleRange(0.0, 40.0), new DoubleRange(0.5, 1.0)),
    FOREST("Forest", 0x249b09, new DoubleRange(0.0, 40.0), new DoubleRange(0.3, 0.8)),
    DESERT("Desert", 0xe2c909, new DoubleRange(0.0, 40.0), new DoubleRange(0.65, 0.95)),
    MOUNTAIN("Mountain", 0xbbbbbb, new DoubleRange(25.0, 50.0), new DoubleRange(0.7, 1.0)),
    NONE("None", Color.BLACK, new DoubleRange(0.0, 0.0), new DoubleRange(0.0, 0.0));

    public static final Set<Biome> LAND_BIOMES = EnumSet.of(BEACH, PLAINS, FOREST, DESERT,
                                                            MOUNTAIN);

    private final String displayName;
    private final Color baseColor;
    private final Range<Double> elevationRange;
    private final Range<Double> valueRange;

    Biome(String displayName, int baseColor, Range<Double> elevationRange,
          Range<Double> valueRange) {
        this(displayName, Funcs.colorFromRgb(baseColor), elevationRange, valueRange);
    }

    Biome(String displayName, Color baseColor, Range<Double> elevationRange,
          Range<Double> valueRange) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.elevationRange = elevationRange;
        this.valueRange = valueRange;
    }

    public boolean isLand() {
        return LAND_BIOMES.contains(this);
    }

    public boolean isWater() {
        return !isLand();
    }

    public String displayName() {
        return displayName;
    }

    public Color baseColor() {
        return baseColor;
    }

    public Color color(int elevation) {
        final float[] hsv = Funcs.toHSV(baseColor());
        // Change the value based on the elevation
        hsv[2] = elevationRange.mapTo((double) elevation, valueRange).floatValue();
        return Funcs.toRGB(hsv);
    }
}
