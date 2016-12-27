package me.lucaspickering.terraingen.world;

import java.awt.Color;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    OCEAN("Ocean", Color.BLUE) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the value based on the elevation
            hsv[2] = World.ELEVATION_RANGE.normalize(elevation, 0.5f, 4f);
            return Funcs.toRGB(hsv);
        }
    },
    PLAINS("Plains", Color.GREEN) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the value based on the elevation
            hsv[2] = 1f - World.ELEVATION_RANGE.normalize(elevation);
            return Funcs.toRGB(hsv);
        }
    },
    MOUNTAIN("Mountain", Color.LIGHT_GRAY) {
        @Override
        public Color color(int elevation) {
            final float[] hsv = Funcs.toHSV(baseColor());
            // Change the value based on the elevation
            hsv[2] = World.ELEVATION_RANGE.normalize(elevation);
            return Funcs.toRGB(hsv);
        }
    };

    private final String displayName;
    private final Color baseColor;

    Biome(String displayName, Color baseColor) {
        this.displayName = displayName;
        this.baseColor = baseColor;
    }

    public String displayName() {
        return displayName;
    }

    public Color baseColor() {
        return baseColor;
    }

    public abstract Color color(int elevation);
}
