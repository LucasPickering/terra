package me.lucaspickering.terraingen.world;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    // You can adjust how much the value of the tile color changes in relation to the elevation.
    // For more value change, use a larger range. You can have the range extend outside [0, 1],
    // and it will be coerced if necessary.
    OCEAN("Ocean", 0x1653b7, new Mapping(-50, -10, 0.3f, 1f)),
    COAST("Coast", 0x1887b2, new Mapping(-20, 0, 0.5f, 1f)),
    LAKE("Lake", 0x09729b, new Mapping(-10, 0, 0.5f, 1f)),
    BEACH("Beach", 0xf2ef59, new Mapping(0, 5, 0.75f, 0.95f)),
    PLAINS("Plains", 0xb9f442, new Mapping(0, 40, 0.5f, 1f)),
    FOREST("Forest", 0x249b09, new Mapping(0, 40, 0.3f, 0.8f)),
    DESERT("Desert", 0xe2c909, new Mapping(0, 40, 0.65f, 0.95f)),
    MOUNTAIN("Mountain", 0xbbbbbb, new Mapping(40, 75, 0.3f, 0.6f)),
    NONE("None", Color.BLACK, new Mapping(0, 0, 0f, 0f));

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

    public static final Set<Biome> LAND_BIOMES = EnumSet.of(BEACH, PLAINS, FOREST, DESERT,
                                                            MOUNTAIN);
    public static final Set<Biome> WATER_BIOMES = EnumSet.of(OCEAN, COAST, LAKE);

    private final String displayName;
    private final Color baseColor;
    private final Mapping colorValueMapping; // Mapping for calculating value of tile color

    Biome(String displayName, int baseColor, Mapping colorValueMapping) {
        this(displayName, Funcs.colorFromRgb(baseColor), colorValueMapping);
    }

    Biome(String displayName, Color baseColor, Mapping colorValueMapping) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.colorValueMapping = colorValueMapping;
    }

    public static boolean isLand(Biome biome) {
        return LAND_BIOMES.contains(biome);
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
        final float value = colorValueMapping.map(elevation);
        hsv[2] = Funcs.coerce(0f, value, 1f); // Coerce value to [0, 1]
        return Funcs.toRGB(hsv);
    }
}
