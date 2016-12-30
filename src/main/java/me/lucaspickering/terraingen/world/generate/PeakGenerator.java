package me.lucaspickering.terraingen.world.generate;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Generates peaks on land/sea, which stick up above other land around.
 */
public class PeakGenerator implements Generator {

    // Generation parameters
    private static final InclusiveRange PEAK_COUNT_RANGE = new InclusiveRange(7, 10);
    private static final InclusiveRange PEAK_ELEVATION_RANGE = new InclusiveRange(45, 60);
    private static final int MIN_PEAK_SEPARATION = 2; // Min distance between two peak
    private static final int SMOOTHING_SLOP = 4; // Variation in each direction for smoothing elev
    private static final int MIN_MOUNTAIN_ELEV = 40; // Everything taller than this is mountain

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        final Map<TilePoint, Tile> tiles = worldBuilder.getTiles();

        // Copy the key set because we're going to be modifying it
        final Set<TilePoint> potentialPeaks = new HashSet<>(tiles.keySet());
        final Set<TilePoint> peaks = new HashSet<>();
        final int peaksToGen = PEAK_COUNT_RANGE.randomIn(random);

        while (peaks.size() < peaksToGen && !potentialPeaks.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final TilePoint peak = Funcs.randomFromCollection(random, potentialPeaks);
            peaks.add(peak); // Add it to the set

            // Get all the tiles that are too close to this one to be peaks themselves,
            // and remove them from the set of potential peaks
            final Set<TilePoint> tooClose = WorldHelper.getTilesInRange(tiles.keySet(), peak,
                                                                        MIN_PEAK_SEPARATION);
            potentialPeaks.removeAll(tooClose);
        }

        for (TilePoint peakPoint : peaks) {
            final Tile peakTile = tiles.get(peakPoint);
            final int peakElev = peakTile.elevation() + PEAK_ELEVATION_RANGE.randomIn(random);
            // Pick a random elevation for the peak and assign it
            setElev(peakTile, peakElev);

            // Adjust the elevation of the adjacent tiles
            for (Map.Entry<Direction, Tile> entry : peakTile.adjacents().entrySet()) {
                final Direction dir = entry.getKey();
                final Tile adjTile = entry.getValue();

                // The tile on the opposite side of adjTile from the peak
                final Tile oppTile = tiles.get(dir.shift(adjTile.pos()));
                final int oppElev = oppTile != null ? oppTile.elevation() : 0;

                // Average peakElev and oppElev, then apply a small random slop
                final int adjElev = Funcs.randomSlop(random, (peakElev + oppElev) / 2,
                                                     SMOOTHING_SLOP);
                setElev(adjTile, adjElev);
            }
        }
    }

    /**
     * Sets the elevation of the given tile to the given value, then sets the tile to be
     * {@link Biome#MOUNTAIN} if appropriate.
     *
     * @param tile      the tile to set
     * @param elevation the elevation that the tile should get
     */
    private void setElev(Tile tile, int elevation) {
        tile.setElevation(elevation);
        if (elevation >= MIN_MOUNTAIN_ELEV) {
            tile.setBiome(Biome.MOUNTAIN);
        }
    }
}
