package me.lucaspickering.terraingen.world.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.world.Tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTileSet {

    @Test
    public void testInitByRadius() throws Exception {
        // Test every size 0-10
        testInitByRadius(0, 10, 1);

        // Test every 10th size 20-100
        testInitByRadius(20, 100, 10);

        // Test every 50th size 150-300
        testInitByRadius(150, 300, 50);
    }

    private void testInitByRadius(int start, int end, int step) {
        for (int size = start; size <= end; size += step) {
            final TileSet tiles = TileSet.initByRadius(size);
            final int expectedSize = 3 * size * (size + 1) + 1; // Mathematically sound
            assertEquals(expectedSize, tiles.size());
        }
    }

    @Test
    public void testAdjacentTiles() throws Exception {
        // Populate a world of tiles for testing
        final int radius = 2;
        final TileSet tiles = TileSet.initByRadius(radius);

        Map<Direction, Tile> result;

        // Center tile has 6 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new HexPoint(0, 0, 0)));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // Another one with 6 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new HexPoint(0, 1, -1)));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // One on the edge with only 3 adjacents
        result = tiles.getAdjacentTiles(tiles.getByPoint(new HexPoint(2, 0, -2)));
        assertEquals("Should have 3 adjacent tiles", 3, result.size());
    }

    @Test
    public void testTilesInRange() throws Exception {
        // Populate a world of tiles for testing
        final int radius = 10;
        final TileSet tiles = TileSet.initByRadius(10);

        final HexPoint origin = HexPoint.ZERO;
        TileSet result;

        // Range of 0 returns just the 1 tile
        result = tiles.getTilesInRange(tiles.getByPoint(origin), 0);
        assertEquals("Should return just 1 tile", 1, result.size());

        // Range of 1 returns the origin and 6 adjacents
        result = tiles.getTilesInRange(tiles.getByPoint(origin), 1);
        assertEquals("Should return 7 tiles", 7, result.size());

        // Range of 2 returns every tile in the world
        result = tiles.getTilesInRange(tiles.getByPoint(origin), radius);
        assertEquals("Should return the entire world", tiles.size(), result.size());
    }

    @Test
    public void testTilesAtDistance() throws Exception {
        final int radius = 10;
        final TileSet tiles = TileSet.initByRadius(radius);

        Tile origin = tiles.getByPoint(HexPoint.ZERO);
        TileSet result;

        // Test each distance in [1, radius]
        for (int r = 0; r < radius; r++) {
            // Run the method we're testing
            result = tiles.getTilesAtDistance(origin, r);

            // Calculate how many tiles we expect (the math checks out)
            final int expectedSize = Tile.NUM_SIDES * r;
            assertEquals("Bad size at radius " + r, expectedSize, result.size());

            // Check that the distance to each tile in the result is correct
            for (Tile tile : result) {
                final int distance = origin.pos().distanceTo(tile.pos());
                assertEquals(String.format("Bad distance between origin and %s at radius %d",
                                           tile, r),
                             r, distance);
            }
        }

        // Make sure a ring outside the collection is empty
        result = tiles.getTilesAtDistance(origin, radius + 1);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCluster() throws Exception {
        final int size = 2;
        final TileSet world = TileSet.initByRadius(size);

        List<Cluster> clusters;

        // Verify that there is one cluster of the entire world
        clusters = world.cluster();
        assertEquals(1, clusters.size());
        assertEquals(world.size(), clusters.get(0).size());

        final TileSet shiftedWorld = new TileSet(world);
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
