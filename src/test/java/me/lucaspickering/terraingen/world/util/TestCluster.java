package me.lucaspickering.terraingen.world.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCluster {

    @Test
    public void testAllAdjacents() throws Exception {
        final TileSet tiles = TileSet.initByRadius(5);
        final Cluster cluster = new Cluster(tiles);
        TileSet adjacents;

        adjacents = cluster.allAdjacents();
        assertTrue("Adjacents should be empty", adjacents.isEmpty());

        cluster.add(tiles.getByPoint(HexPoint.ZERO));
        adjacents = cluster.allAdjacents();
        assertEquals(6, adjacents.size());

        cluster.add(tiles.getByPoint(new HexPoint(1, -1, 0)));
        adjacents = cluster.allAdjacents();
        assertEquals(8, adjacents.size());

        cluster.removePoint(HexPoint.ZERO);
        adjacents = cluster.allAdjacents();
        assertEquals(6, adjacents.size());

//        final Cluster clone = new Cluster(cluster);
//        assertEquals(cluster.allAdjacents(), clone.allAdjacents());
    }
}
