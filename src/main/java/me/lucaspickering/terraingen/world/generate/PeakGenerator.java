package me.lucaspickering.terraingen.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Generates peaks on land/sea, which stick up above other land around.
 */
public class PeakGenerator implements Generator {

    private static final IntRange PEAK_COUNT_RANGE = new IntRange(7, 10);
    private static final IntRange PEAK_ELEVATION_RANGE =
        new IntRange(15, World.ELEVATION_RANGE.max());
    private static final int MIN_PEAK_SEPARATION = 3; // Min distance between two peaks
    private static final int SMOOTHING_SLOP = 4; // Variation in each direction for smoothing elev

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();
        final int peaksToGen = PEAK_COUNT_RANGE.randomIn(random);
        final TileSet peaks = worldTiles.selectTiles(random, peaksToGen, MIN_PEAK_SEPARATION);

        for (Tile peak : peaks) {
            final int peakElev = peak.elevation() + PEAK_ELEVATION_RANGE.randomIn(random);
            // Pick a random elevation for the peak and assign it
            setElev(peak, peakElev);

            // Adjust the elevation of the adjacent tiles
            for (Map.Entry<Direction, Tile> entry : worldTiles.getAdjacentTiles(peak).entrySet()) {
                final Direction dir = entry.getKey();
                final Tile adjTile = entry.getValue();

                // The tile on the opposite side of adjTile from the peak
                final Tile oppTile = worldTiles.getByPoint(dir.shift(adjTile.pos()));
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
        if (elevation >= PEAK_ELEVATION_RANGE.min()) {
            tile.setBiome(Biome.MOUNTAIN);
        }
    }
}
