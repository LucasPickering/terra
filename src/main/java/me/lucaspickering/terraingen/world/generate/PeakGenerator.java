package me.lucaspickering.terraingen.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileSet;

/**
 * Generates peaks on land/sea, which stick up above other land around.
 */
public class PeakGenerator implements Generator {

    private static final IntRange PEAK_COUNT_RANGE = new IntRange(7, 10);
    private static final IntRange PEAK_ELEVATION_RANGE =
        new IntRange(15, World.ELEVATION_RANGE.max());
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    private static final int SMOOTHING_SLOP = 4; // Variation in each direction for smoothing elev
    private static final int PROPAGATION_RANGE = 5;

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();
        final int peaksToGen = PEAK_COUNT_RANGE.randomIn(random);
        final TileSet peaks = worldTiles.selectTiles(random, peaksToGen, MIN_PEAK_SEPARATION);

        for (Tile peak : peaks) {
            int elev = PEAK_ELEVATION_RANGE.randomIn(random);

            for (int dist = 0; dist <= PROPAGATION_RANGE; dist++) {
                final TileSet tilesAtRange = worldTiles.getTilesAtDistance(peak, dist);
                for (Tile tile : tilesAtRange) {
                    final int sloppedElev = Funcs.randomSlop(random, elev, SMOOTHING_SLOP);
                    addElev(tile, sloppedElev);
                }

                elev /= 2;
            }
        }

        final List<Cluster> continentClusters =
            worldTiles.cluster(tile -> tile.elevation() >= World.SEA_LEVEL).first();
        for (Cluster cluster : continentClusters) {
            world.getContinents().add(new Continent(cluster));
        }
    }

    /**
     * Sets the elevation of the given tile to the given value, then sets the tile to be
     * {@link Biome#MOUNTAIN} if appropriate.
     *
     * @param tile      the tile to set
     * @param elevation the elevation that the tile should get
     */
    private void addElev(Tile tile, int elevation) {
        System.out.printf("Setting elev to %d%n", elevation);
        tile.setElevation(tile.elevation() + elevation);
        if (elevation >= PEAK_ELEVATION_RANGE.min()) {
            tile.setBiome(Biome.MOUNTAIN);
        }
    }
}
