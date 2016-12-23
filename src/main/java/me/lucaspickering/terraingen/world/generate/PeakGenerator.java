package me.lucaspickering.terraingen.world.generate;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

public class PeakGenerator implements Generator {

    // Generation parameters
    private static final InclusiveRange PEAK_COUNT_RANGE = new InclusiveRange(4, 7);
    private static final InclusiveRange PEAK_ELEVATION_RANGE = new InclusiveRange(45, 60);
    private static final int MIN_PEAK_SEPARATION = 2; // Min distance between two peak

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        final Map<TilePoint, Tile.Builder> builders = worldBuilder.builders();

        // Copy the key set because we're going to be modifying it
        final Set<TilePoint> potentialPeaks = new HashSet<>(builders.keySet());
        final Set<TilePoint> peaks = new HashSet<>();
        final int peaksToGen = PEAK_COUNT_RANGE.randomIn(random);

        while (peaks.size() < peaksToGen && !potentialPeaks.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final TilePoint peak = Funcs.randomFromCollection(random, potentialPeaks);
            peaks.add(peak); // Add it to the set

            // Get all the tiles that are too close to this one to be peaks themselves,
            // and remove them from the set of potential peaks
            final Set<TilePoint> tooClose = WorldHelper.getTilesInRange(builders.keySet(), peak,
                                                                        MIN_PEAK_SEPARATION);
            potentialPeaks.removeAll(tooClose);
        }

        for (TilePoint peak : peaks) {
            final Tile.Builder peakBuilder = builders.get(peak);
            final int elev = PEAK_ELEVATION_RANGE.randomIn(random);
            // Pick a random elevation for the peak and assign it
            peakBuilder.setElevation(elev);

            // Adjust the elevation of the adjacent tiles
            for (Tile.Builder adjBuilder : peakBuilder.getAdjacents().values()) {
                final int adjElev = PEAK_ELEVATION_RANGE.randomIn(random) - 20;
                adjBuilder.setElevation(adjElev);
            }
        }
    }
}
