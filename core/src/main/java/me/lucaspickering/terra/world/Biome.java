package me.lucaspickering.terra.world;

import com.badlogic.gdx.graphics.Color;

public enum Biome {

    // All colors are RGBA

    OCEAN("Ocean", 0x144ba4ff, Type.WATER),
    COAST("Coast", 0x398bc6ff, Type.WATER),
    LAKE("Lake", 0x0b8cbeff, Type.WATER),

    // The order of these matters because it determines the priority with which they are assigned
    // to tiles.
    SNOW("Snow", 0xbbbbbbff, Type.LAND),
    DESERT("Desert", 0xd7cb6cff, Type.LAND),
    ALPINE("Alpine", 0x637a5eff, Type.LAND),
    JUNGLE("Jungle", 0x2bb31eff, Type.LAND),
    FOREST("Forest", 0x177a00ff, Type.LAND),
    PLAINS("Plains", 0xadc974ff, Type.LAND),

    BEACH("Beach", 0xf2ef59ff, Type.LAND),
    CLIFF("Cliff", 0x634936ff, Type.LAND),

    NONE("None", 0x000000ff, Type.NONE); // Placeholder biome

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
