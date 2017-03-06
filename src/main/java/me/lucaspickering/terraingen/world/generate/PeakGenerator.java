package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TileSet;

/**
 * Generates peaks and raises the tiles around them,
 */
public class PeakGenerator implements Generator {

    private static final int TILES_PER_PEAK = 2000;
    private static final IntRange PEAK_ELEVATION_RANGE = new IntRange(15,
                                                                      World.ELEVATION_RANGE.max());
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    private static final int PROPAGATION_RANGE = 30; // Radius of tiles that each peak raises
    private static final float PEAK_SLOPE = 2f; // Larger values make elev drop off more quickly
    private static final int FLOOR_ELEV = World.ELEVATION_RANGE.min() / 2;

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        // Init all tiles to be at the floor elevation
        worldTiles.forEach(tile -> tile.setElevation(FLOOR_ELEV));

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
    }

    private int getElevFromDistance(int distance, int peakElev) {
        return Math.max(peakElev - (int) (distance * PEAK_SLOPE), FLOOR_ELEV);
    }
}
