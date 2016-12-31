package me.lucaspickering.terraingen.world;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;
import static org.junit.Assert.assertEquals;

public class TestWorldHelper {

    @Test
    public void testTileToPixel() throws Exception {
        final TilePoint origin = new TilePoint(0, 0, 0);
        final TilePoint tile1 = new TilePoint(1, 2, -3);
        final TilePoint tile2 = new TilePoint(3, -3, 0);

        Point expected;

        // Simple case
        expected = Point.ZERO;
        assertEquals(expected, WorldHelper.tileToPixel(origin));

        // Unfortunately I can't think an alternative to hardcoding these numbers without
        // basically copying the code we're testing
        expected = new Point(111, -320);
        assertEquals(expected, WorldHelper.tileToPixel(tile1));

        expected = new Point(333, 192);
        assertEquals(expected, WorldHelper.tileToPixel(tile2));
    }

    @Test
    public void testPixelToTile() throws Exception {
        final Point origin = Point.ZERO;

        // Just barely in the origin tile
        final Point p1 = new Point(0, -Tile.HEIGHT / 2 + 1);
        // On the edge - counts as origin
        final Point p2 = new Point(0, -Tile.HEIGHT / 2);
        // Just barely outside the origin tile
        final Point p3 = new Point(0, -Tile.HEIGHT / 2 - 1);

        final Point p4 = new Point(-80, -70); // On the fringes of the tile

        // Just left of left corner of origin tile
        final Point p5 = new Point(-Tile.WIDTH / 2 - 1, 0);
        // Right onleft corner of origin tile
        final Point p6 = new Point(-Tile.WIDTH / 2, 0);
        // Just right of left corner of origin tile
        final Point p7 = new Point(-Tile.WIDTH / 2 + 1, 0);

        TilePoint expected;

        // Simple case
        expected = new TilePoint(0, 0, 0);
        assertEquals(expected, WorldHelper.pixelToTile(origin));

        expected = new TilePoint(0, 0, 0);
        assertEquals(expected, WorldHelper.pixelToTile(p1));
        expected = new TilePoint(0, 0, 0);
        assertEquals(expected, WorldHelper.pixelToTile(p2));
        expected = new TilePoint(0, 1, -1);
        assertEquals(expected, WorldHelper.pixelToTile(p3));

        expected = new TilePoint(-1, 1, 0);
        assertEquals(expected, WorldHelper.pixelToTile(p4));

        expected = new TilePoint(-1, 0, 1);
        assertEquals(expected, WorldHelper.pixelToTile(p5));
        expected = new TilePoint(0, 0, 0);
        assertEquals(expected, WorldHelper.pixelToTile(p6));
        expected = new TilePoint(0, 0, 0);
        assertEquals(expected, WorldHelper.pixelToTile(p7));
    }

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
        result = WorldHelper.getAdjacentTiles(tiles, new TilePoint(0, 0, 0));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // Another one with 6 adjacents
        result = WorldHelper.getAdjacentTiles(tiles, new TilePoint(0, 1, -1));
        assertEquals("Should have 6 adjacent tiles", 6, result.size());

        // One on the edge with only 3 adjacents
        result = WorldHelper.getAdjacentTiles(tiles, new TilePoint(2, 0, -2));
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
        result = WorldHelper.getTilesInRange(tiles, origin, 0);
        assertEquals("Should return just 1 tile", 1, result.size());

        // Range of 1 returns the origin and 6 adjacents
        result = WorldHelper.getTilesInRange(tiles, origin, 1);
        assertEquals("Should return 7 tiles", 7, result.size());

        // Range of 2 returns every tile in the world
        result = WorldHelper.getTilesInRange(tiles, origin, 2);
        assertEquals("Should return the entire world", tiles.size(), result.size());
    }
}
