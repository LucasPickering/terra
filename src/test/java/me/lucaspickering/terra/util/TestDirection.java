package me.lucaspickering.terra.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

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
}
