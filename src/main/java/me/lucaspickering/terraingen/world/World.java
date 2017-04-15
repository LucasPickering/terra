package me.lucaspickering.terraingen.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.lucaspickering.terraingen.world.util.Chunk;
import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.terraingen.world.util.TileMap;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * A simple container for holding information about a world. The main portion of this class is
 * the {@link TileSet} instance that stores all tiles in the world, but it also holds
 * useful information such as continents, constants for world-related values, etc.
 */
public class World {

    /**
     * Every tile's elevation must be in this range
     */
    public static final Range<Integer> ELEVATION_RANGE = new IntRange(-50, 50);
    public static final Range<Double> HUMIDITY_RANGE = new DoubleRange(0.0, 1.0);

    /**
     * Any tile below, but not equal to, this elevation can feasibly become ocean tiles. Most
     * land tiles will be at or above this elevation.
     */
    public static final int SEA_LEVEL = 0;

    private final TileSet tiles;
    private final Map<HexPoint, Chunk> chunks;
    private final List<Continent> continents;
    private final TileMap<Continent> tilesToContinents;

    public World(int radius) {
        tiles = TileSet.initByRadius(radius);
        chunks = new TreeMap<>();
        continents = new ArrayList<>();
        tilesToContinents = new TileMap<>();
        initChunks();
    }

    /**
     * Copy constructor.
     */
    private World(TileSet tiles, Map<HexPoint, Chunk> chunks, List<Continent> continents,
                  TileMap<Continent> tilesToContinents) {
        this.tiles = tiles;
        this.chunks = chunks;
        this.continents = continents;
        this.tilesToContinents = tilesToContinents;
    }

    /**
     * Initializes all chunks in the world, so that each tile belongs to exactly one chunk.
     */
    private void initChunks() {
        for (Tile tile : tiles) {
            final HexPoint chunkPos = Chunk.getChunkPosForTile(tile.pos());

            final Chunk chunk;
            if (chunks.containsKey(chunkPos)) {
                chunk = chunks.get(chunkPos);
            } else {
                chunk = new Chunk(chunkPos);
                chunks.put(chunkPos, chunk);
            }

            chunk.addTile(tile);
        }
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
        return new World(tiles.immutableCopy(), Collections.unmodifiableMap(chunks),
                         Collections.unmodifiableList(continents), // NO DEEP COPY
                         tilesToContinents.immutableCopy()); // NO DEEP COPY
    }
}
