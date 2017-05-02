package me.lucaspickering.terra.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terra.world.Biome;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.Cluster;
import me.lucaspickering.utils.GeneralFuncs;

/**
 * Generates oceans. Calculates all clusters of tiles that are below sea level, then turns those
 * clusters into oceans. If a cluster is above a certain size threshold, it is guaranteed to be
 * an ocean. Otherwise, it has a random chance to become an ocean, with that chance being
 * proportional to its size.
 */
public class OceanGenerator extends Generator {

    private static final int MIN_OCEAN_SIZE = 50; // Minimum size to be considered an ocean
    private static final int MIN_COAST_DEPTH = -5; // Everything in an ocean >= this is coast

    public OceanGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        // Get clusters of tiles that are at or below sea level
        final List<Cluster> clusters =
            Cluster.categoryCluster(world().getTiles(), t -> t.elevation() < World.SEA_LEVEL).get(true);

        for (Cluster cluster : clusters) {
            // 1 tile has 0 chance, 2 tiles have 1/x chance, 3 tiles have 2/x chance, etc.
            final float chance = (float) (cluster.size() - 1) / (MIN_OCEAN_SIZE - 2);

            // If the cluster is above the size threshold, or the we randomly select it to be an
            // ocean, set all tiles in it to be coast/ocean
            if (cluster.size() >= MIN_OCEAN_SIZE || GeneralFuncs.weightedChance(random(), chance)) {
                makeOcean(cluster);
            }
        }
    }

    private void makeOcean(Cluster cluster) {
        for (Tile tile : cluster) {
            // If this tile is shallow, make it coast, otherwise make it ocean
            tile.setBiome(tile.elevation() >= MIN_COAST_DEPTH ? Biome.COAST : Biome.OCEAN);
        }
    }
}
