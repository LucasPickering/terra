package me.lucaspickering.terra.util;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import me.lucaspickering.terra.world.util.HexPoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestHexPoint {

    @Test
    public void testDistance() throws Exception {
        final HexPoint origin = new HexPoint(0, 0, 0);
        final HexPoint tile1 = new HexPoint(-1, 0, 1);
        final HexPoint tile2 = new HexPoint(0, -5, 5);
        final HexPoint tile3 = new HexPoint(-3, 2, 1);

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

    @Test
    public void testEquals() throws Exception {
        final HexPoint point = new HexPoint(-1, 1, 0);
        assertFalse(point.equals(HexPoint.ZERO));
        assertTrue(point.equals(-1, 1, 0));
        assertFalse(point.equals(1, -1, 1));
    }
}
