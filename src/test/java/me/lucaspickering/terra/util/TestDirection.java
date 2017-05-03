package me.lucaspickering.terra.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDirection {

    @Test
    public void testOpposite() throws Throwable {
        assertEquals(Direction.NORTH.opposite(), Direction.SOUTH);
        assertEquals(Direction.NORTHEAST.opposite(), Direction.SOUTHWEST);
        assertEquals(Direction.SOUTHEAST.opposite(), Direction.NORTHWEST);
        assertEquals(Direction.SOUTH.opposite(), Direction.NORTH);
        assertEquals(Direction.SOUTHWEST.opposite(), Direction.NORTHEAST);
        assertEquals(Direction.NORTHWEST.opposite(), Direction.SOUTHEAST);
    }

    @Test
    public void testIsAdjacentTo() throws Throwable {
        assertFalse(Direction.NORTH.isAdjacentTo(Direction.NORTH));
        assertTrue(Direction.NORTH.isAdjacentTo(Direction.NORTHEAST));
        assertFalse(Direction.NORTH.isAdjacentTo(Direction.SOUTHEAST));
        assertFalse(Direction.NORTH.isAdjacentTo(Direction.SOUTH));
        assertFalse(Direction.NORTH.isAdjacentTo(Direction.SOUTHWEST));
        assertTrue(Direction.NORTH.isAdjacentTo(Direction.NORTHWEST));

        assertTrue(Direction.NORTHEAST.isAdjacentTo(Direction.NORTH));
        assertFalse(Direction.NORTHEAST.isAdjacentTo(Direction.NORTHEAST));
        assertTrue(Direction.NORTHEAST.isAdjacentTo(Direction.SOUTHEAST));
        assertFalse(Direction.NORTHEAST.isAdjacentTo(Direction.SOUTH));
        assertFalse(Direction.NORTHEAST.isAdjacentTo(Direction.SOUTHWEST));
        assertFalse(Direction.NORTHEAST.isAdjacentTo(Direction.NORTHWEST));

        assertFalse(Direction.SOUTHEAST.isAdjacentTo(Direction.NORTH));
        assertTrue(Direction.SOUTHEAST.isAdjacentTo(Direction.NORTHEAST));
        assertFalse(Direction.SOUTHEAST.isAdjacentTo(Direction.SOUTHEAST));
        assertTrue(Direction.SOUTHEAST.isAdjacentTo(Direction.SOUTH));
        assertFalse(Direction.SOUTHEAST.isAdjacentTo(Direction.SOUTHWEST));
        assertFalse(Direction.SOUTHEAST.isAdjacentTo(Direction.NORTHWEST));

        assertFalse(Direction.SOUTH.isAdjacentTo(Direction.NORTH));
        assertFalse(Direction.SOUTH.isAdjacentTo(Direction.NORTHEAST));
        assertTrue(Direction.SOUTH.isAdjacentTo(Direction.SOUTHEAST));
        assertFalse(Direction.SOUTH.isAdjacentTo(Direction.SOUTH));
        assertTrue(Direction.SOUTH.isAdjacentTo(Direction.SOUTHWEST));
        assertFalse(Direction.SOUTH.isAdjacentTo(Direction.NORTHWEST));

        assertFalse(Direction.SOUTHWEST.isAdjacentTo(Direction.NORTH));
        assertFalse(Direction.SOUTHWEST.isAdjacentTo(Direction.NORTHEAST));
        assertFalse(Direction.SOUTHWEST.isAdjacentTo(Direction.SOUTHEAST));
        assertTrue(Direction.SOUTHWEST.isAdjacentTo(Direction.SOUTH));
        assertFalse(Direction.SOUTHWEST.isAdjacentTo(Direction.SOUTHWEST));
        assertTrue(Direction.SOUTHWEST.isAdjacentTo(Direction.NORTHWEST));

        assertTrue(Direction.NORTHWEST.isAdjacentTo(Direction.NORTH));
        assertFalse(Direction.NORTHWEST.isAdjacentTo(Direction.NORTHEAST));
        assertFalse(Direction.NORTHWEST.isAdjacentTo(Direction.SOUTHEAST));
        assertFalse(Direction.NORTHWEST.isAdjacentTo(Direction.SOUTH));
        assertTrue(Direction.NORTHWEST.isAdjacentTo(Direction.SOUTHWEST));
        assertFalse(Direction.NORTHWEST.isAdjacentTo(Direction.NORTHWEST));
    }
}
