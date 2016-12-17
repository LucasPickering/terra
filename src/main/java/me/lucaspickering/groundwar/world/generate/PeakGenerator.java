package me.lucaspickering.groundwar.world.generate;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.groundwar.util.Funcs;
import me.lucaspickering.groundwar.util.InclusiveRange;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.WorldHelper;
import me.lucaspickering.groundwar.world.tile.Tile;

public class PeakGenerator implements Generator {

    // Generation parameters
    private static final InclusiveRange PEAKS_RANGE = new InclusiveRange(3, 5);
    private static final int MIN_PEAK_SEPARATION = 2; // Min distance between two peak

    @Override
    public void generate(Map<TilePoint, Tile.Builder> world, Random random) {
        // Generate peaks
        // Copy the key set because we're going to be modifying it
        final Set<TilePoint> potentialPeaks = new HashSet<>(world.keySet());
        final Set<TilePoint> peaks = new HashSet<>();
        final int peaksToGen = PEAKS_RANGE.randomIn(random);

        while (peaks.size() < peaksToGen && !potentialPeaks.isEmpty()) {
            // Pick a random peak from the set of potential peaks
            final TilePoint peak = Funcs.randomFromCollection(random, potentialPeaks);
            peaks.add(peak); // Add it to the set

            // Get all the tiles that are too close to this one to be peaks themselves,
            // and remove them from the set of potential peaks
            final Set<TilePoint> tooClose = WorldHelper.getTilesInRange(world.keySet(), peak,
                                                                        MIN_PEAK_SEPARATION);
            potentialPeaks.removeAll(tooClose);
        }

        for (TilePoint peak : peaks) {
            world.get(peak).setElevation(5); // Modify the peak tile
        }
    }
}
