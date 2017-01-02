package me.lucaspickering.terraingen.world;

import org.junit.Test;

import me.lucaspickering.terraingen.util.TilePoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCluster {

    @Test
    public void testAllAdjacents() throws Exception {
        final Tiles tiles = WorldHelper.initTiles(5);
        final Cluster cluster = new Cluster();
        Tiles adjacents;

        adjacents = cluster.allAdjacents();
        assertTrue("Adjacents should be empty", adjacents.isEmpty());

        cluster.add(tiles.getByPoint(TilePoint.ZERO));
        adjacents = cluster.allAdjacents();
        assertEquals(6, adjacents.size());

        cluster.add(tiles.getByPoint(new TilePoint(1, -1, 0)));
        adjacents = cluster.allAdjacents();
        assertEquals(8, adjacents.size());

        // Not supported for nwo
//        cluster.removePoint(TilePoint.ZERO);
//        adjacents = cluster.allAdjacents();
//        assertEquals(6, adjacents.size());

        final Cluster clone = new Cluster(cluster);
        assertEquals(cluster.allAdjacents(), clone.allAdjacents());
    }
}
