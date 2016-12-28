package me.lucaspickering.terraingen.world.generate;

import java.util.List;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.WorldBuilder;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Paint tiles to be oceans and lakes. All tiles with negative elevation become candidates for
 * having water. Clusters of negative tiles over a certain size are guaranteed to become oceans.
 * All other clusters have a chance of becoming lakes. The larger the cluster, the higher the
 * chance.
 */
public class WaterPainter implements Generator {

    // Generation parameters
    // Minimum size of a cluster to be considered an ocean
    private static final int MIN_OCEAN_SIZE = 10;

    @Override
    public void generate(WorldBuilder worldBuilder, Random random) {
        // Get clusters of tiles that have negative elevation
        final List<Map<TilePoint, Tile>> clusters =
            WorldHelper.clusterTiles(worldBuilder.getTiles(), t -> t.elevation() < 0);

        for (Map<TilePoint, Tile> cluster : clusters) {
            // If this cluster is over the min ocean size, make everything in it an ocean
            if (cluster.size() >= MIN_OCEAN_SIZE) {
                for (Tile tile : cluster.values()) {
                    tile.setBiome(Biome.OCEAN);
                }
            }
            // TODO lakes
        }
    }
}
