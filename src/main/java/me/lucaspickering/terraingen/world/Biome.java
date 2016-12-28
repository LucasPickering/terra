package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    OCEAN("Ocean", Color.BLUE, false) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the value based on the elevation
            hsv[2] = World.LOWER_ELEVATION_RANGE.normalize(elevation, 0.3f, 1f);
            return Funcs.toRGB(hsv);
        }
    },
    BEACH("Beach", Color.YELLOW, true) {
        @Override
        public Color color(int elevation) {
            return baseColor();
        }
    },
    PLAINS("Plains", Color.GREEN) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the value based on the elevation
            hsv[2] = World.UPPER_ELEVATION_RANGE.normalize(elevation, 0.6f, 1f);
            return Funcs.toRGB(hsv);
        }
    },
    ALPINE("Alpine", Funcs.colorFromRgb(0x00bb00)) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the saturation and value based on the elevation
            hsv[1] = 1f - World.UPPER_ELEVATION_RANGE.normalize(elevation, 0.2f, 0.8f);
            hsv[2] = World.UPPER_ELEVATION_RANGE.normalize(elevation, 0.2f, 0.8f);
            return Funcs.toRGB(hsv);
        }
    };

    private final String displayName;
    private final Color baseColor;
    private final boolean isLand;

    Biome(String displayName, Color baseColor) {
        this(displayName, baseColor, true);
    }

    Biome(String displayName, Color baseColor, boolean isLand) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.isLand = isLand;
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

    public abstract Color color(int elevation);
}
