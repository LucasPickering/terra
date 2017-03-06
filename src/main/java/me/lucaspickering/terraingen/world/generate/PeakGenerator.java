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

    private static final int TILES_PER_PEAK = 20;
    private static final IntRange PEAK_ELEVATION_RANGE =
        new IntRange(25, 40);
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    private static final int ELEV_SLOP = 0; // Variation in each direction for each elev
    private static final int PROPAGATION_RANGE = 30;
    private static final float PEAK_SLOPE = 0.7f; // Smaller values make elev drop off more quickly

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();
        final int peaksToGen = worldTiles.size() / TILES_PER_PEAK + 1;
        final TileSet peaks = worldTiles.selectTiles(random, peaksToGen, MIN_PEAK_SEPARATION);

        for (Tile peak : peaks) {
            int nominalElev = PEAK_ELEVATION_RANGE.randomIn(random);

            for (int dist = 0; dist <= PROPAGATION_RANGE; dist++) {
                final TileSet tilesAtRange = worldTiles.getTilesAtDistance(peak, dist);
                for (Tile tile : tilesAtRange) {
                    final int sloppedElev = Funcs.randomSlop(random, nominalElev, ELEV_SLOP);
                    setElev(tile, sloppedElev);
                }

                // Reduce the nominal elevation for the next distance step by a factor
                nominalElev *= PEAK_SLOPE;
            }
        }

        final List<Cluster> continentClusters =
            worldTiles.cluster(tile -> tile.elevation() >= World.SEA_LEVEL).first();
        for (Cluster cluster : continentClusters) {
            world.getContinents().add(new Continent(cluster));
        }
    }

    /**
     * Adds the given elevation to the tile's existing elevation, then sets the tile to be
     * {@link Biome#MOUNTAIN} if appropriate.
     *
     * @param tile      the tile to set
     * @param elevDelta the elevation to add
     */
    private void setElev(Tile tile, int elevDelta) {
        final int elevation = tile.elevation() + elevDelta;
        tile.setElevation(elevation);
        if (elevation >= PEAK_ELEVATION_RANGE.min()) {
            tile.setBiome(Biome.MOUNTAIN);
        }
    }
}
