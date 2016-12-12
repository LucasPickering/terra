package me.lucaspickering.groundwar.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.groundwar.TerrainGen;
import me.lucaspickering.groundwar.util.Funcs;
import me.lucaspickering.groundwar.util.InclusiveRange;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public class World {

    // Board size
    private static final int X_SIZE = 5, Y_SIZE = 5, Z_SIZE = 5;

    // Generation parameters
    private static final InclusiveRange PEAKS_RANGE = new InclusiveRange(3, 5);
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    // How many times we can fail to generate a valid peak before giving up
    private static final int FAILED_PEAK_THRESHOLD = 10; //

    private final Random random;
    private final Map<TilePoint, Tile> tiles;

    public World() {
        random = TerrainGen.instance().random();
        tiles = new HashMap<>();
        genTiles();
    }

    private void genTiles() {
        // Temporary map to hole Tile builders until they're ready to be built
        final Map<TilePoint, Tile.Builder> builders = new HashMap<>();
        // Fill out the board with builders, to be populated with biome/elev/etc. later on
        for (int x = -X_SIZE; x <= X_SIZE; x++) {
            for (int y = -Y_SIZE; y <= Y_SIZE; y++) {
                for (int z = -Z_SIZE; z <= Z_SIZE; z++) {
                    if (x + y + z == 0) {
                        final TilePoint pos = new TilePoint(x, y, z);
                        // todo remove biome setting here
                        builders.put(pos, Tile.Builder.fromPos(pos).biome(Biome.PLAINS));
                    }
                }
            }
        }

        // Generate peaks
        final Set<TilePoint> tilePoints = builders.keySet();
        final Set<TilePoint> peaks = new HashSet<>();
        final int peaksToGen = PEAKS_RANGE.randomIn(random);
        int failedPeaks = 0; // Number of invalid peaks we've generated

        while (peaks.size() < peaksToGen && failedPeaks < FAILED_PEAK_THRESHOLD) {
            final TilePoint peak = Funcs.randomFromCollection(random, tilePoints);

            // If this peak is too close to any other peak, call it a failure and try again
            boolean valid = true;
            for (TilePoint otherPeak : peaks) {
                if (peak.distanceTo(otherPeak) < MIN_PEAK_SEPARATION) {
                    failedPeaks++; // Count this as a failure
                    valid = false;
                    break; // Don't need to check any more peaks
                }
            }

            // If this peak is valid
            if (valid) {
                peaks.add(peak); // This peak is valid, so add it to the set
            }
        }

        for (TilePoint peak : peaks) {
            builders.get(peak).biome(Biome.DEBUG).elevation(5); // Modify the peak tile
        }

        // Build each tile and put it into the final map
        builders.forEach((pos, builder) -> tiles.put(pos, builder.build()));
    }

    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }
}
