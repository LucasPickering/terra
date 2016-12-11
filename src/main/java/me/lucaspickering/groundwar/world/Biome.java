package me.lucaspickering.groundwar.world;

import java.awt.Color;

import me.lucaspickering.groundwar.util.Colors;

public enum Biome {

    PLAINS(Colors.TILE_BG);

    private final Color color;

    Biome(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
