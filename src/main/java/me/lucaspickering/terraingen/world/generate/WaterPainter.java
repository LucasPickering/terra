package me.lucaspickering.terraingen.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Cluster;
import me.lucaspickering.terraingen.world.WorldHandler;
import me.lucaspickering.terraingen.world.World;
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
    public static final int MIN_OCEAN_SIZE = 20;
    private static final int MIN_COAST_DEPTH = -10; // Everything in an ocean >= this is coast

    @Override
    public void generate(World world, Random random) {
        // Get clusters of tiles that are at or below sea level
        final List<Cluster> clusters = world.getTiles()
            .cluster(t -> t.elevation() < WorldHandler.SEA_LEVEL).first();

        for (Cluster cluster : clusters) {
            final int size = cluster.size();

            // If this cluster is over the min ocean size...
            if (size >= MIN_OCEAN_SIZE) {
                // Make everything in it an coast/ocean
                for (Tile tile : cluster) {
                    // If this tile is shallow, make it coast, otherwise make it ocean
                    tile.setBiome(tile.elevation() >= MIN_COAST_DEPTH ? Biome.COAST : Biome.OCEAN);
                }
            } else {
                // Otherwise, give it a chance to become a lake, proportional to its size
                // 1 tile has 0 chance, 2 tiles have 1/x chance, 3 tiles have 2/x chance, etc.
                final float chance = (float) (size - 1) / (MIN_OCEAN_SIZE - 2);
                if (random.nextFloat() < chance) {
                    // If we chose to make it a lake, change all the tiles to lake
                    cluster.forEach(tile -> tile.setBiome(Biome.LAKE));
                }
            }
        }
    }
}
