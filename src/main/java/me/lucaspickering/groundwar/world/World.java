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
    private static final int MIN_PEAK_SEPARATION = 2; // Min distance between two peaks

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
        // Copy the key set because we're going to be modifying it
        final Set<TilePoint> potentialPeaks = new HashSet<>(builders.keySet());
        final Set<TilePoint> peaks = new HashSet<>();
        final int peaksToGen = PEAKS_RANGE.randomIn(random);

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
            builders.get(peak).biome(Biome.DEBUG).elevation(5); // Modify the peak tile
        }

        // Build each tile and put it into the final map
        builders.forEach((pos, builder) -> tiles.put(pos, builder.build()));
    }

    /**
     * Gets the world's copy of tiles. This is NOT a copy, so DO NOT MODIFY IT.
     *
     * @return the world's copy of tiles
     */
    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }
}
