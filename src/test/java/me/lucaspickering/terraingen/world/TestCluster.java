package me.lucaspickering.terraingen.world;

import org.junit.Test;

import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileSet;
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

        cluster.add(tiles.getByPoint(TilePoint.ZERO));
        adjacents = cluster.allAdjacents();
        assertEquals(6, adjacents.size());

        cluster.add(tiles.getByPoint(new TilePoint(1, -1, 0)));
        adjacents = cluster.allAdjacents();
        assertEquals(8, adjacents.size());

        cluster.removePoint(TilePoint.ZERO);
        adjacents = cluster.allAdjacents();
        assertEquals(6, adjacents.size());

        final Cluster clone = new Cluster(cluster);
        assertEquals(cluster.allAdjacents(), clone.allAdjacents());
    }
}
