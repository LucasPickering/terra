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

    private final Map<HexPoint, Chunk> chunks;
    private final List<Continent> continents;
    private final TileMap<Continent> tilesToContinents;

    public World(int chunkRadius) {
        chunks = initChunks(chunkRadius);
        continents = new ArrayList<>();
        tilesToContinents = new TileMap<>();
    }

    /**
     * Copy constructor.
     */
    private World(Map<HexPoint, Chunk> chunks, List<Continent> continents,
                  TileMap<Continent> tilesToContinents) {
        this.chunks = chunks;
        this.continents = continents;
        this.tilesToContinents = tilesToContinents;
    }

    /**
     * Initializes all chunks in the world, so that each tile belongs to exactly one chunk.
     */
    private Map<HexPoint, Chunk> initChunks(int radius) {
        final Map<HexPoint, Chunk> result = new TreeMap<>();

        // Iterate over x and y to create a "circle" of chunks with the given radius
        for (int x = -radius; x <= radius; x++) {

            // Calculate the min and max y values that a chunk in this range can have
            final int minY = Math.max(-radius, -x - radius);
            final int maxY = Math.min(radius, -x + radius);
            for (int y = minY; y <= maxY; y++) {
                // Create a chunk at this location
                final HexPoint pos = new HexPoint(x, y);
                final Chunk chunk = Chunk.createChunkWithTiles(pos);
                result.put(pos, chunk);
            }
        }

        return result;
    }

    public List<Continent> getContinents() {
        return continents;
    }

    public TileMap<Continent> getTilesToContinents() {
        return tilesToContinents;
    }

    public World immutableCopy() {
        return new World(Collections.unmodifiableMap(chunks), // NO DEEP COPY
                         Collections.unmodifiableList(continents), // NO DEEP COPY
                         tilesToContinents.immutableCopy()); // NO DEEP COPY
    }
}
