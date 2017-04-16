package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import me.lucaspickering.terraingen.world.util.Chunk;
import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.terraingen.world.util.HexPointSet;
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
     * This is an abstraction that allows a user to iterate over all tiles in the world easily.
     * The tiles are stored independently in different chunks, so this class iterates over all
     * tiles in one chunk, then moves onto the next chunk, and so on. Tiles cannot be added or
     * removed from this set.
     */
    private class WorldTiles extends TileSet {

        private WorldTiles() {
            // Give the super class a null map to make sure we don't rely on that map for any
            // operations. If we did, it would just be empty anyway, which could cause spoopy bugs.
            super((Map<HexPoint, Tile>) null);
        }

        @Override
        public Tile getByPoint(HexPoint point) {
            // Find the chunk that contains the given point, then get the tile from that chunk
            final HexPoint chunkPos = Chunk.getChunkPosForTile(point);
            final Chunk chunk = chunks.getByPoint(chunkPos);
            if (chunk != null) {
                return chunk.getTiles().getByPoint(point);
            }
            return null; // The chunk doesn't exist, therefore the tile doesn't exist
        }

        @Override
        public int size() {
            // Chunks have a constant size so we can calculate the total number of tiles from the
            // total number of chunks
            return chunks.size() * Chunk.CHUNK_SIZE;
        }

        @NotNull
        @Override
        public Iterator<Tile> iterator() {
            return new WorldTilesIterator();
        }

        @Override
        public boolean add(Tile tile) {
            throw new UnsupportedOperationException(); // Cannot add tiles
        }

        @Override
        public boolean removeByPoint(HexPoint point) {
            throw new UnsupportedOperationException(); // Cannot remove tiles
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(); // Cannot remove tiles
        }

        // Potential optimization? Override getAdjacentTiles and make it chunk-aware, so that it
        // only looks for tiles outside the current chunk if it needs to.
    }

    private class WorldTilesIterator implements Iterator<Tile> {

        private final Iterator<Chunk> chunkIterator;
        private Iterator<Tile> tileIterator;

        private WorldTilesIterator() {
            chunkIterator = chunks.iterator();
        }

        @Override
        public boolean hasNext() {
            // If there is another tile in this chunk, or there's another chunk after this one,
            // then there is a next iteration.
            return (tileIterator != null && tileIterator.hasNext()) || chunkIterator.hasNext();
        }

        @Override
        public Tile next() {
            // If this tile iterator is out of tiles, move onto the next chunk
            if (tileIterator == null || !tileIterator.hasNext()) {
                // If there is no next chunk, throw an exception
                if (!chunkIterator.hasNext()) {
                    throw new NoSuchElementException();
                }

                // Get the next chunk, and get a tile iterator for it
                final Chunk nextChunk = chunkIterator.next();
                tileIterator = nextChunk.getTiles().iterator();
            }
            return tileIterator.next(); // Return the next tile
        }
    }

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

    private final HexPointSet<Chunk> chunks;
    private final WorldTiles worldTiles = new WorldTiles();
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
    private World(HexPointSet<Chunk> chunks, List<Continent> continents,
                  TileMap<Continent> tilesToContinents) {
        this.chunks = chunks;
        this.continents = continents;
        this.tilesToContinents = tilesToContinents;
    }

    /**
     * Initializes all chunks in the world, so that each tile belongs to exactly one chunk.
     */
    private HexPointSet<Chunk> initChunks(int radius) {
        final HexPointSet<Chunk> result = new HexPointSet<>();

        // Iterate over x and y to create a "circle" of chunks with the given radius
        for (int x = -radius; x <= radius; x++) {

            // Calculate the min and max y values that a chunk in this range can have
            final int minY = Math.max(-radius, -x - radius);
            final int maxY = Math.min(radius, -x + radius);
            for (int y = minY; y <= maxY; y++) {
                // Create a chunk at this location
                final HexPoint pos = new HexPoint(x, y);
                result.add(Chunk.createChunkWithTiles(pos));
            }
        }

        return result;
    }

    public Map<HexPoint, Chunk> getChunks() {
        return chunks;
    }

    public TileSet getTiles() {
        return worldTiles;
    }

    public List<Continent> getContinents() {
        return continents;
    }

    public TileMap<Continent> getTilesToContinents() {
        return tilesToContinents;
    }

    public World immutableCopy() {
        return new World(chunks.immutableCopy(), // NO DEEP COPY
                         Collections.unmodifiableList(continents), // NO DEEP COPY
                         tilesToContinents.immutableCopy()); // NO DEEP COPY
    }
}
