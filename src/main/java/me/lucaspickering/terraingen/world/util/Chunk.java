package me.lucaspickering.terraingen.world.util;

import java.util.Set;

public class Chunk {

    public static final int CHUNK_RADIUS = 50;

    private final TileSet tiles;

    public Chunk(TileSet tiles) {
        this.tiles = tiles.immutableCopy();
    }

    public TileSet getTiles() {
        return tiles;
    }

    public Set<HexPoint> getPointsInChunk() {
        return null; // TODO
    }
}
