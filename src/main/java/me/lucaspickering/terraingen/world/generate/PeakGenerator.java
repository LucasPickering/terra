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
    private static final float PEAK_SLOPE = 0.25f; // Larger values make elev drop off more quickly
    private static final int FLOOR_ELEV = World.ELEVATION_RANGE.min() / 2;

    /**
     * Represents one mathematical function that, given distance from the peak, returns the
     * elevation for a tile at the distance. The equation is configured with the elevation of the
     * peak and a slope factor, to allow for the equation to vary from peak to peak.
     */
    private static class ElevationCalculator {

        private final int peakElev;
        private final float factor;

        private ElevationCalculator(int peakElev, float factor) {
            this.peakElev = peakElev;
            this.factor = factor;
        }

        private int getElev(int distance) {
            final int elev = peakElev / ((int) (factor * distance) + 1);
            return Math.max(elev, FLOOR_ELEV); // Make sure nothing less than the floor is returned
        }
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        // Init all tiles to be at the floor elevation
        worldTiles.forEach(tile -> tile.setElevation(FLOOR_ELEV));

        final int peaksToGen = worldTiles.size() / TILES_PER_PEAK + 1;
        final TileSet peaks = worldTiles.selectTiles(random, peaksToGen, MIN_PEAK_SEPARATION);

        for (Tile peak : peaks) {
            final int peakElev = PEAK_ELEVATION_RANGE.randomIn(random);
            final ElevationCalculator eq = new ElevationCalculator(peakElev, PEAK_SLOPE);

            for (int dist = 0; dist <= PROPAGATION_RANGE; dist++) {
                // Get all tiles <dist> away from the peak, then calculate the desired elev for them
                final TileSet tilesAtRange = worldTiles.getTilesAtDistance(peak, dist);
                final int elev = eq.getElev(dist);

                // Set the elev of each tile, but only if it would go up
                for (Tile tile : tilesAtRange) {
                    if (tile.elevation() < elev) {
                        tile.setElevation(elev);
                    }
                }
            }
        }
    }
}
