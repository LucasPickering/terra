package me.lucaspickering.terraingen.world.generate;

import java.util.List;
import java.util.Random;

import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.Cluster;
import me.lucaspickering.terraingen.world.util.TileMap;

/**
 * Clusters all land tiles in the world together to become continents. This should probably be
 * used after everything that modifies elevation is done, so that you know shorelines won't
 * change anymore. It should obviously be done before any operations that rely on having accurate
 * continent clusters.
 */
public class ContinentClusterer implements Generator {

    @Override
    public void generate(World world, Random random) {
        // Cluster tiles based on whether they're above sea level or not
        final List<Cluster> continentClusters =
            world.getTiles().cluster(tile -> tile.biome().isLand()).first();

        final List<Continent> continents = world.getContinents();
        final TileMap<Continent> tilesToContinents = world.getTilesToContinents();

        // Clear the containers so they can be repopulated
        continents.clear();
        tilesToContinents.clear();

        for (Cluster cluster : continentClusters) {
            final Continent continent = new Continent(cluster);
            continents.add(continent); // Add the new continent to the list

            // Put each tile in the continent into our map
            for (Tile tile : cluster) {
                tilesToContinents.put(tile, continent);
            }
        }
    }
}
