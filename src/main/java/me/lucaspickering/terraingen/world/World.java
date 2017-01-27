package me.lucaspickering.terraingen.world;

import java.util.List;

import me.lucaspickering.terraingen.util.IntRange;

/**
 * A simple container for holding information about a world. The main portion of this class is
 * the {@link Tiles} instance that stores all tiles in the world, but it also holds
 * useful information such as continents, constants for world-related values, etc.
 */
public class World {

    /**
     * Every tile's elevation must be in this range
     */
    public static final IntRange ELEVATION_RANGE = new IntRange(-25, 25);

    /**
     * Any tile below, but not equal to, this elevation can feasibly become ocean tiles. Most
     * land tiles will be at or above this elevation.
     */
    public static final int SEA_LEVEL = 0;

    private final Tiles tiles;
    private List<Cluster> continents;

    public World(int radius) {
        tiles = Tiles.initByRadius(radius);
    }

    /**
     * Copy constructor
     */
    private World(Tiles tiles, List<Cluster> continents) {
        this.tiles = tiles;
        this.continents = continents;
    }

    public Tiles getTiles() {
        return tiles;
    }

    public List<Cluster> getContinents() {
        return continents;
    }

    public void setContinents(List<Cluster> continents) {
        this.continents = continents;
    }

    public World immutableCopy() {
        // TODO Make continents immutable in this
        return new World(tiles.immutableCopy(), continents);
    }
}
