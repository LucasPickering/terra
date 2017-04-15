package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Set;
import java.util.TreeSet;

import me.lucaspickering.terraingen.world.Tile;

public class Chunk implements Comparable<Chunk> {

    public static final int CHUNK_SIZE = 10;
    public static final Chunk ZERO = new Chunk(HexPoint.ZERO);

    private final HexPoint pos; // Position of this chunk relative to other chunks
    private final TileSet tiles = new TileSet();
    private final Color overlayColor;
    private final Set<HexPoint> points = new TreeSet<>();

    public Chunk(HexPoint pos) {
        this.pos = pos;
        final int x = Math.abs(pos.x());
        final int y = Math.abs(pos.y());
        final int z = Math.abs(pos.z());
        overlayColor = new Color(x * 50 % 256, y * 50 % 256, z * 50 % 256);
        System.out.println(overlayColor);
    }

    public static HexPoint getChunkPosForTile(HexPoint tilePos) {
        return HexPoint.ZERO; //TODO
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

    public Set<HexPoint> getPointsInChunk() {
        if (points.isEmpty()) {
            for (int x = pos.x(); x < pos.x() + CHUNK_SIZE; x++) {
                for (int y = pos.y(); y < pos.y() + CHUNK_SIZE; y++) {
                    points.add(new HexPoint(x, y));
                }
            }
        }

        return points;
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
