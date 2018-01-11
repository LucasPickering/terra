package me.lucaspickering.terra.world;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointSet;
import me.lucaspickering.terra.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * A simple container for holding information about a world. The main portion of this class is the
 * {@link TileSet} instance that stores all tiles in the world, but it also holds useful information
 * such as continents, constants for world-related values, etc.
 */
public class World {

    /**
     * This is an abstraction that allows a user to iterate over all tiles in the world easily. The
     * tiles are stored independently in different chunks, so this class iterates over all tiles in
     * one chunk, then moves onto the next chunk, and so on. Tiles cannot be added or removed from
     * this set.
     */
    private class ChunkedTileSet extends TileSet {

        private ChunkedTileSet() {
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
            return chunks.size() * Chunk.TOTAL_TILES;
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

        /**
         * Gets the set of all tiles adjacent to the given tile. This is an optimized version of
         * {@link TileSet#getAdjacentTiles(HexPoint)}. The tiles in this container are divided by
         * chunk. For a large majority of lookups, all adjacent tiles will be in the same chunk as
         * the given ("host") tile. We can avoid doing unnecessary chunk lookups by first checking
         * if the adjacent tile is in the same chunk as the host tile.
         *
         * @param tilePos the center of the search
         * @return tiles adjacent to {@code tile}, in a direction:point map
         */
        @Override
        @NotNull
        public Map<Direction, Tile> getAdjacentTiles(@NotNull HexPoint tilePos) {
            Objects.requireNonNull(tilePos);
            final Map<Direction, Tile> result = new EnumMap<>(Direction.class);
            final Chunk hostChunk = chunks.getByPoint(Chunk.getChunkPosForTile(tilePos));

            for (Direction dir : Direction.values()) {
                final HexPoint otherPoint = dir.shift(tilePos); // Get the shifted point
                final Tile otherTile;

                // If the other tile is in the same chunk as the given tile, we already have that
                // chunk on-hand so just look it up from there. Otherwise, look it up normally,
                // which means it looks up the chunk, then the tile.
                if (Chunk.getChunkPosForTile(otherPoint).equals(hostChunk.getPos())) {
                    otherTile = hostChunk.getTiles().getByPoint(otherPoint);
                } else {
                    otherTile = getByPoint(otherPoint);
                }

                // If the other point is in the world, add it to the map
                if (otherTile != null) {
                    result.put(dir, otherTile);
                }
            }

            return result;
        }
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
    public static final Range<Integer> ELEVATION_RANGE = new IntRange(-1000, 1000);
    public static final Range<Double> HUMIDITY_RANGE = new DoubleRange(0.0, 1.0);

    /**
     * Any tile below, but not equal to, this elevation can feasibly become ocean tiles. Most land
     * tiles will be at or above this elevation.
     */
    public static final int SEA_LEVEL = 0;

    private final long seed;
    private final HexPointSet<Chunk> chunks;
    private final ChunkedTileSet worldTiles = new ChunkedTileSet();
    private final List<Continent> continents;

    public World(long seed, int chunkRadius) {
        this.seed = seed;
        chunks = initChunks(chunkRadius);
        continents = new ArrayList<>();
    }

    /**
     * Copy constructor.
     */
    private World(long seed, HexPointSet<Chunk> chunks, List<Continent> continents) {
        this.seed = seed;
        this.chunks = chunks;
        this.continents = continents;
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

    public long getSeed() {
        return seed;
    }

    public HexPointSet<Chunk> getChunks() {
        return chunks;
    }

    public TileSet getTiles() {
        return worldTiles;
    }

    public List<Continent> getContinents() {
        return continents;
    }

    public World immutableCopy() {
        return new World(seed,
                         chunks.immutableCopy(), // NO DEEP COPY
                         Collections.unmodifiableList(continents)); // NO DEEP COPY
    }
}
