package me.lucaspickering.terraingen.world;

import org.junit.Test;

import java.util.List;
import java.util.Map;

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

        Map<Direction, Tile> result;

        // Center tile has 6 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new TilePoint(0, 0, 0)));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // Another one with 6 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new TilePoint(0, 1, -1)));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // One on the edge with only 3 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new TilePoint(2, 0, -2)));
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
        Tiles result;

        // Range of 0 returns just the 1 tile
        result = tiles.getTilesInRange(tiles.getByPoint(origin), 0);
        assertEquals("Should return just 1 tile", 1, result.size());

        // Range of 1 returns the origin and 6 adjacents
        result = tiles.getTilesInRange(tiles.getByPoint(origin), 1);
        assertEquals("Should return 7 tiles", 7, result.size());

        // Range of 2 returns every tile in the world
        result = tiles.getTilesInRange(tiles.getByPoint(origin), 2);
        assertEquals("Should return the entire world", tiles.size(), result.size());
    }

    @Test
    public void testCluster() throws Exception {
        final int size = 2;
        final Tiles world = WorldHelper.initTiles(size);

        List<Cluster> clusters;

        // Verify that there is one cluster of the entire world
        clusters = world.cluster();
        assertEquals(1, clusters.size());
        assertEquals(world.size(), clusters.get(0).size());

        final Tiles shiftedWorld = new Tiles(world);
        final int shift = size * 3;
        for (Tile tile : world) {
            shiftedWorld.add(new Tile(tile.pos().plus(shift, -shift, 0)));
        }

        clusters = shiftedWorld.cluster();
        assertEquals(2, clusters.size());
        assertEquals(world.size(), clusters.get(0).size());
        assertEquals(world.size(), clusters.get(1).size());
    }
}
