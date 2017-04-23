package me.lucaspickering.terraingen.world;

import java.awt.Color;

public enum Biome {

    OCEAN("Ocean", 0x144ba4, Type.WATER),
    COAST("Coast", 0x398bc6, Type.WATER),
    LAKE("Lake", 0xff0000, Type.WATER),
//    LAKE("Lake", 0x0b8cbe, Type.WATER),

    // The order of these matters because it determines the priority with which they are assigned
    // to tiles.
    SNOW("Snow", 0xbbbbbb, Type.LAND),
    DESERT("Desert", 0xd7cb6c, Type.LAND),
    ALPINE("Alpine", 0x637a5e, Type.LAND),
    JUNGLE("Jungle", 0x2bb31e, Type.LAND),
    FOREST("Forest", 0x177a00, Type.LAND),
    PLAINS("Plains", 0xadc974, Type.LAND),

    BEACH("Beach", 0xf2ef59, Type.LAND),

    NONE("None", 0x000000, Type.NONE);

    private enum Type {
        LAND, WATER, NONE
    }

    private final String displayName;
    private final Color color;
    private final Type type;

    Biome(String displayName, int color, Type type) {
        this.displayName = displayName;
        this.color = new Color(color);
        this.type = type;
    }

    public boolean isLand() {
        return type == Type.LAND;
    }

    public boolean isWater() {
        return type == Type.WATER;
    }

    public String displayName() {
        return displayName;
    }

    public Color color() {
        return color;
    }
}
