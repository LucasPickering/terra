package me.lucaspickering.terraingen.world;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;

public enum Biome {

    OCEAN("Ocean", 0x144ba4),
    COAST("Coast", 0x398bc6),
    LAKE("Lake", 0x09729b),
    BEACH("Beach", 0xf2ef59),
    PLAINS("Plains", 0xadc974),
    FOREST("Forest", 0x249b09),
    DESERT("Desert", 0xd7d093),
    SNOW("Snow", 0xeeeeee),
    NONE("None", Color.BLACK);

    public static final Set<Biome> LAND_BIOMES = EnumSet.of(BEACH, PLAINS, FOREST, DESERT,
                                                            SNOW);

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
