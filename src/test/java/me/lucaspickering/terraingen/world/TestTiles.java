package me.lucaspickering.terraingen.world;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;
import static org.junit.Assert.assertEquals;

public class TestTiles {

    @Test
    public void testAdjacentTiles() throws Exception {
        // Populate a world of tiles for testing
        final int radius = 2;
        final Tiles tiles = new Tiles();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x + y + z == 0) {
                        tiles.add(new Tile(new TilePoint(x, y, z)));
                    }
                }
            }
        }

        Map<Direction, TilePoint> result;

        // Center tile has 6 adjacents
        result = tiles.getAdjacentTiles(new TilePoint(0, 0, 0));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // Another one with 6 adjacents
        result = tiles.getAdjacentTiles(new TilePoint(0, 1, -1));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // One on the edge with only 3 adjacents
        result = tiles.getAdjacentTiles(new TilePoint(2, 0, -2));
        assertEquals("Should have 3 adjacent tiles", 3, result.size());
    }

    @Test
    public void testTilesInRange() throws Exception {
        // Populate a world of tiles for testing
        final int radius = 2;
        final Tiles tiles = new Tiles();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x + y + z == 0) {
                        tiles.add(new Tile(new TilePoint(x, y, z)));
                    }
                }
            }
        }

        final TilePoint origin = new TilePoint(0, 0, 0);
        Set<TilePoint> result;

        // Range of 0 returns just the 1 tile
        result = tiles.getTilesInRange(origin, 0);
        assertEquals("Should return just 1 tile", 1, result.size());

        // Range of 1 returns the origin and 6 adjacents
        result = tiles.getTilesInRange(origin, 1);
        assertEquals("Should return 7 tiles", 7, result.size());

        // Range of 2 returns every tile in the world
        result = tiles.getTilesInRange(origin, 2);
        assertEquals("Should return the entire world", tiles.size(), result.size());
    }
}
