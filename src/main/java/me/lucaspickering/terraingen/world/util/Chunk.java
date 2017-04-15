package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import me.lucaspickering.terraingen.world.Tile;

/**
 * A Chunk is a set of tiles of a set size that makes up a portion of the world. The
 * shape of a chunk is a parallelogram, with the length of each side of the chunk being
 * {@link #CHUNK_SIDE_LENGTH} tiles. A chunk has the following properties:
 * <ul>
 * <li>It cannot be empty</li>
 * <li>There are always exactly {@link #CHUNK_SIZE} tiles in a chunk</li>
 * <li>Tiles cannot be added to or removed from the chunk after initialization</li>
 * <li>A tile that is in this chunk cannot be in any other chunk</li>
 * </ul>
 */
public class Chunk implements Comparable<Chunk> {

    public static final int CHUNK_SIDE_LENGTH = 50;
    public static final int CHUNK_SIZE = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;

    private static final int OVERLAY_RGB_FACTOR = 50;
    private static final int OVERLAY_ALPHA = 10;

    private final HexPoint pos; // Position of this chunk relative to other chunks
    private final TileSet tiles;
    private final Color overlayColor;

    private Chunk(HexPoint pos) {
        this.pos = pos;
        tiles = new TileSet();
        overlayColor = new Color(pos.x() * OVERLAY_RGB_FACTOR & 0xff,
                                 pos.y() * OVERLAY_RGB_FACTOR & 0xff,
                                 pos.z() * OVERLAY_RGB_FACTOR & 0xff,
                                 OVERLAY_ALPHA);
    }

    /**
     * Copy constructor
     */
    private Chunk(HexPoint pos, TileSet tiles, Color overlayColor) {
        this.pos = pos;
        this.tiles = tiles;
        this.overlayColor = overlayColor;
    }

    /**
     * Creates an immutable chunk at the given position. Each tile that belongs in the chunk is
     * constructed. The chunk is immutable in that no tiles can be added or removed, but the
     * tiles themselves are mutable. Each tile in the chunk will be initialized so that it
     * belongs to this chunk.
     *
     * @param pos the position of the chunk
     * @return the created chunk
     */
    public static Chunk createChunkWithTiles(HexPoint pos) {
        final Chunk chunk = new Chunk(pos);
        final int startX = pos.x() * CHUNK_SIDE_LENGTH;
        final int startY = pos.y() * CHUNK_SIDE_LENGTH;
        for (int x = startX; x < startX + CHUNK_SIDE_LENGTH; x++) {
            for (int y = startY; y < startY + CHUNK_SIDE_LENGTH; y++) {
                final HexPoint tilePos = new HexPoint(x, y);
                final Tile tile = new Tile(tilePos, chunk);
                chunk.tiles.add(tile);
                tile.setChunk(chunk);
            }
        }
        assert chunk.tiles.size() == CHUNK_SIZE;
        return chunk.immutableCopy();
    }

    /**
     * Converts the given tile position to the position of the chunk to which that tile should
     * belong.
     *
     * @param tilePos the position of the tile
     * @return the position of the chunk that should contain that tile
     */
    public static HexPoint getChunkPosForTile(HexPoint tilePos) {
        final int x = Math.floorDiv(tilePos.x(), CHUNK_SIDE_LENGTH);
        final int y = Math.floorDiv(tilePos.y(), CHUNK_SIDE_LENGTH);
        return new HexPoint(x, y);
    }

    public HexPoint getPos() {
        return pos;
    }

    public TileSet getTiles() {
        return tiles;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }

    public Chunk immutableCopy() {
        return new Chunk(pos, tiles.immutableCopy(), overlayColor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Chunk chunk = (Chunk) o;

        return pos.equals(chunk.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public int compareTo(@NotNull Chunk o) {
        return pos.compareTo(o.pos);
    }
}
