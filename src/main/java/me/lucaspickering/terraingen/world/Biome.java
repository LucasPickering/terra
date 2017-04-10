package me.lucaspickering.terraingen.world;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    OCEAN("Ocean", 0x1653b7),
    COAST("Coast", 0x1887b2),
    LAKE("Lake", 0x09729b),
    BEACH("Beach", 0xf2ef59),
    PLAINS("Plains", 0xb9f442),
    FOREST("Forest", 0x249b09),
    DESERT("Desert", 0xe2c909),
    MOUNTAIN("Mountain", 0xbbbbbb),
    NONE("None", Color.BLACK);

    public static final Set<Biome> LAND_BIOMES = EnumSet.of(BEACH, PLAINS, FOREST, DESERT,
                                                            MOUNTAIN);

    private final String displayName;
    private final Color color;

    Biome(String displayName, int color) {
        this(displayName, Funcs.colorFromRgb(color));
    }

    Biome(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
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

    public Color color() {
        return color;
    }
}
