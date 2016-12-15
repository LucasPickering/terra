package me.lucaspickering.groundwar.world;

import java.awt.Color;

import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Funcs;

public enum Biome {

    PLAINS(Colors.TILE_BG), MOUNTAIN(Funcs.gray(0xbb)), DEBUG(Color.RED);

    private final Color color;

    Biome(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
