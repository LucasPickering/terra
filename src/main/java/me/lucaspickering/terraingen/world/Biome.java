package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    // You can adjust how much the value of the tile color changes in relation to the elevation.
    // For more value change, use a larger range. You can have the range extend outside [0, 1],
    // and it will be coerced if necessary.
    OCEAN("Ocean", 0x1653b7, false, new Mapping(-50, -10, 0.3f, 1f)),
    COAST("Coast", 0x1887b2, false, new Mapping(-20, 0, 0.5f, 1f)),
    LAKE("Lake", 0x09729b, false, new Mapping(-10, 0, 0.5f, 1f)),
    BEACH("Beach", 0xe2c909, true, new Mapping(0, 5, 0.75f, 0.95f)),
    PLAINS("Plains", 0xb9f442, true, new Mapping(0, 40, 0.5f, 1f)),
    FOREST("Forest", 0x249b09, true, new Mapping(0, 40, 0.3f, 0.8f)),
    MOUNTAIN("Mountain", 0xbbbbbb, true, new Mapping(40, 75, 0.3f, 0.6f));

    private static class Mapping {

        private final float fromMin;
        private final float fromMax;
        private final float toMin;
        private final float toMax;

        private Mapping(float fromMin, float fromMax, float toMin, float toMax) {
            this.fromMin = fromMin;
            this.fromMax = fromMax;
            this.toMin = toMin;
            this.toMax = toMax;
        }

        private float map(float x) {
            return Funcs.mapToRange(fromMin, fromMax, toMin, toMax, x);
        }
    }

    private final String displayName;
    private final Color baseColor;
    private final boolean isLand;
    private final Mapping colorValueMapping; // Mapping for calculating value of tile color

    Biome(String displayName, int baseColor, boolean isLand, Mapping colorValueMapping) {
        this(displayName, Funcs.colorFromRgb(baseColor), isLand, colorValueMapping);
    }

    Biome(String displayName, Color baseColor, boolean isLand, Mapping colorValueMapping) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.isLand = isLand;
        this.colorValueMapping = colorValueMapping;
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
        final float value = colorValueMapping.map(elevation);
        hsv[2] = Funcs.coerce(0f, value, 1f); // Coerce value to [0, 1]
        return Funcs.toRGB(hsv);
    }
}
