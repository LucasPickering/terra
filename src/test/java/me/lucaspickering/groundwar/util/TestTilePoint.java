package me.lucaspickering.groundwar.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTilePoint {

    @Test
    public void testDistance() throws Exception {
        final TilePoint origin = new TilePoint(0, 0, 0);
        final TilePoint tile1 = new TilePoint(-1, 0, 1);
        final TilePoint tile2 = new TilePoint(0, -5, 5);
        final TilePoint tile3 = new TilePoint(-3, 2, 1);

        // Test reflexivity
        assertEquals(0, origin.distanceTo(origin));

        // Test distance from origin to each other tile, also testing commutativity
        assertEquals(1, origin.distanceTo(tile1));
        assertEquals(5, origin.distanceTo(tile2));
        assertEquals(3, origin.distanceTo(tile3));

        assertEquals(1, tile1.distanceTo(origin));
        assertEquals(5, tile1.distanceTo(tile2));
        assertEquals(2, tile1.distanceTo(tile3));

        assertEquals(5, tile2.distanceTo(origin));
        assertEquals(5, tile2.distanceTo(tile1));
        assertEquals(7, tile2.distanceTo(tile3));

        assertEquals(3, tile3.distanceTo(origin));
        assertEquals(2, tile3.distanceTo(tile1));
        assertEquals(7, tile3.distanceTo(tile2));
    }
}
