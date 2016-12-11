package me.lucaspickering.groundwar.world;

import me.lucaspickering.groundwar.util.Colors;

public enum Biome {

    PLAINS(Colors.TILE_BG);

    private final int color;

    Biome(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
