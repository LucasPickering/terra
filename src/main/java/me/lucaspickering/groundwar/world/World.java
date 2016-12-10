package me.lucaspickering.groundwar.world;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.world.tile.Tile;

public class World {

    private final Map<Point, Tile> tiles;

    private final Random random = new Random();

    public World() {
        tiles = null; // todo generate world here
    }

    public Map<Point, Tile> getTiles() {
        return tiles;
    }
}
