package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    // You can adjust how much the value of the tile color changes in relation to the elevation.
    // For more value change, use a larger range. You can have the range extend outside [0, 1],
    // and it will be coerced if necessary.
    OCEAN("Ocean", 0x1653b7, false, 0.3f, 1f),
    COAST("Coast", 0x1887b2, false, 0.3f, 1f),
    LAKE("Lake", 0x09729b, false, 0.3f, 1f),
    BEACH("Beach", 0xe2c909, true, -1.25f, 3.75f),
    PLAINS("Plains", 0xb9f442, true, 0.5f, 0.9f),
    FOREST("Forest", 0x249b09, true, 0.4f, 0.8f),
    MOUNTAIN("Mountain", 0xbbbbbb, true, 0f, 0.8f);

    private final String displayName;
    private final Color baseColor;
    private final boolean isLand;
    private final float minValue, maxValue; // Min and max value for the color of this biome

    Biome(String displayName, int baseColor, boolean isLand, float minValue, float maxValue) {
        this(displayName, Funcs.colorFromRgb(baseColor), isLand, minValue, maxValue);
    }

    Biome(String displayName, Color baseColor, boolean isLand, float minValue, float maxValue) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.isLand = isLand;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String displayName() {
        return displayName;
    }

    public Color baseColor() {
        return baseColor;
    }

    public boolean isLand() {
        return isLand;
    }

    public Color color(int elevation) {
        final float[] hsv = Funcs.toHSV(baseColor());
        // Change the value based on the elevation
        hsv[2] = World.ELEVATION_RANGE.normalize(elevation, minValue, maxValue);
        hsv[2] = Funcs.coerce(0f, hsv[2], 1f); // Max sure it's [0, 1]
        return Funcs.toRGB(hsv);
    }
}
