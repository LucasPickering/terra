package me.lucaspickering.terraingen.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileSet;

/**
 * Generates peaks on land/sea, which stick up above other land around.
 */
public class PeakGenerator implements Generator {

    private static final int TILES_PER_PEAK = 2000;
    private static final IntRange PEAK_ELEVATION_RANGE =
        new IntRange(25, World.ELEVATION_RANGE.max());
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    private static final int PROPAGATION_RANGE = 30;
    private static final float PEAK_SLOPE = 2f; // Larger values make elev drop off more quickly
    private static final int FLOOR_ELEV = World.ELEVATION_RANGE.min();

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();
        worldTiles.forEach(tile -> tile.setElevation(World.ELEVATION_RANGE.min()));

        final int peaksToGen = worldTiles.size() / TILES_PER_PEAK + 1;
        final TileSet peaks = worldTiles.selectTiles(random, peaksToGen, MIN_PEAK_SEPARATION);

        for (Tile peak : peaks) {
            final int peakElev = PEAK_ELEVATION_RANGE.randomIn(random);

            for (int dist = 0; dist <= PROPAGATION_RANGE; dist++) {
                // Get all tiles dist away from the peak, then calculate the desired elev for them
                final TileSet tilesAtRange = worldTiles.getTilesAtDistance(peak, dist);
                final int elev = getElevFromDistance(dist, peakElev);

                // Set the elev of each tile, but only if it would go up
                for (Tile tile : tilesAtRange) {
                    if (tile.elevation() < elev) {
                        tile.setElevation(elev);
                    }
                }
            }
        }

        // Cluster land that's above sea level into continents
        final List<Cluster> continentClusters =
            worldTiles.cluster(tile -> tile.elevation() >= World.SEA_LEVEL).first();
        for (Cluster cluster : continentClusters) {
            world.getContinents().add(new Continent(cluster));
        }
    }

    private int getElevFromDistance(int distance, int peakElev) {
        return Math.max(peakElev - (int) (distance * PEAK_SLOPE), FLOOR_ELEV);
    }
}
