package me.lucaspickering.terra.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.Cluster;

/**
 * Clusters all land tiles in the world together to become continents. This should probably be
 * used after everything that modifies elevation is done, so that you know shorelines won't
 * change anymore. It should obviously be done before any operations that rely on having accurate
 * continent clusters.
 */
public class ContinentClusterer extends Generator {

    public ContinentClusterer(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        // Cluster tiles based on whether they're land or not
        final List<Cluster> continentClusters =
            Cluster.cluster(world().getTiles(), tile -> tile.biome().isLand()).first();

        final List<Continent> continents = world().getContinents();

        // Clear the containers so they can be repopulated
        continents.clear();

        for (Cluster cluster : continentClusters) {
            final Continent continent = new Continent(cluster);
            continents.add(continent); // Add the new continent to the list

            // Put each tile in the continent into our map
            for (Tile tile : cluster) {
                tile.setContinent(continent);
            }
        }
    }
}
