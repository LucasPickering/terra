package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import me.lucaspickering.terraingen.world.Tile;

public class Chunk implements Comparable<Chunk> {

    public static final int CHUNK_SIZE = 50;

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

    public static Chunk createChunkWithTiles(HexPoint pos) {
        final Chunk chunk = new Chunk(pos);
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                final HexPoint tilePos = new HexPoint(i * pos.x(), j * pos.y());
                chunk.addTile(new Tile(tilePos, chunk));
            }
        }
        return chunk;
    }

    /**
     * Converts the given tile position to the position of the chunk to which that tile should
     * belong.
     *
     * @param tilePos the position of the tile
     * @return the position of the chunk that should contain that tile
     */
    public static HexPoint getChunkPosForTile(HexPoint tilePos) {
        final int x = Math.floorDiv(tilePos.x(), CHUNK_SIZE);
        final int y = Math.floorDiv(tilePos.y(), CHUNK_SIZE);
        return new HexPoint(x, y);
    }

    public HexPoint getPos() {
        return pos;
    }

    public TileSet getTiles() {
        return tiles;
    }

    /**
     * Adds the given tile to this chunk. Also sets the tile's chunk to be this. This cannot be
     * done if the tile is already in a chunk.
     *
     * @param tile the tile to add to this chunk
     * @throws IllegalArgumentException if the tile is already in a chunk (even if it is already in
     *                                  this chunk)
     */
    public void addTile(Tile tile) {
        if (tile.getChunk() != null) {
            throw new IllegalArgumentException("This tile is already in a chunk!");
        }
        tiles.add(tile);
        tile.setChunk(this);
    }

    public Color getOverlayColor() {
        return overlayColor;
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
