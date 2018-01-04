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
 * clusters into oceans. If a cluster is above a certain size threshold, it is guaranteed to be an
 * ocean. Otherwise, it has a random chance to become an ocean, with that chance being proportional
 * to its size.
 */
public class OceanGenerator extends Generator {

    private static final int MIN_OCEAN_SIZE = 15; // Minimum size to be possibly become an ocean
    private static final int MIN_GUARANTEED_OCEAN_SIZE = 50; // Min size to be guaranteed an ocean
    private static final int MIN_COAST_DEPTH = -5; // Everything in an ocean >= this is coast

    public OceanGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        // Get clusters of tiles that are at or below sea level
        final List<Cluster> clusters =
            Cluster.predicateCluster(world().getTiles(), t -> t.elevation() < World.SEA_LEVEL);

        // We use this every iteration so calculate it now. See below for explanation of math.
        final float chanceDenom = MIN_GUARANTEED_OCEAN_SIZE - MIN_OCEAN_SIZE + 1;

        for (Cluster cluster : clusters) {
            // Calculate the chance of this cluster becoming an ocean. For this example, let's
            // say MIN_OCEAN_SIZE=15 and MIN_GUARANTEED_OCEAN_SIZE=50. Clusters of size 14 and
            // below have 0 chance of being an ocean. Size 15 has a 1/36 chance, size 16 has
            // 2/36, etc. up to 49->35/36 size, and 50 has 100% chance.
            final float chance = (cluster.size() - MIN_OCEAN_SIZE + 1) / chanceDenom;

            // Decide if this should be an ocean
            if (GeneralFuncs.weightedChance(random(), chance)) {
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
