package me.lucaspickering.terraingen.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.util.TileMap;
import me.lucaspickering.terraingen.world.util.TileSet;

/**
 * A simple container for holding information about a world. The main portion of this class is
 * the {@link TileSet} instance that stores all tiles in the world, but it also holds
 * useful information such as continents, constants for world-related values, etc.
 */
public class World {

    /**
     * Every tile's elevation must be in this range
     */
    public static final IntRange ELEVATION_RANGE = new IntRange(-25, 50);

    /**
     * Any tile below, but not equal to, this elevation can feasibly become ocean tiles. Most
     * land tiles will be at or above this elevation.
     */
    public static final int SEA_LEVEL = 0;

    private final TileSet tiles;
    private final List<Continent> continents;
    private final TileMap<Continent> tilesToContinents;

    public World(int radius) {
        tiles = TileSet.initByRadius(radius);
        continents = new ArrayList<>();
        tilesToContinents = new TileMap<>();
    }

    /**
     * Copy constructor.
     */
    private World(TileSet tiles, List<Continent> continents, TileMap<Continent> tilesToContinents) {
        this.tiles = tiles;
        this.continents = continents;
        this.tilesToContinents = tilesToContinents;
    }

    public TileSet getTiles() {
        return tiles;
    }

    public List<Continent> getContinents() {
        return continents;
    }

    public TileMap<Continent> getTilesToContinents() {
        return tilesToContinents;
    }

    public World immutableCopy() {
        return new World(tiles.immutableCopy(),
                         Collections.unmodifiableList(continents), // NO DEEP COPY
                         tilesToContinents.immutableCopy()); // NO DEEP COPY
    }
}
