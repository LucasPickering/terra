package me.lucaspickering.terraingen.world.generate;

import java.util.Random;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;

/**
 * Paints biomes onto each continent. Without adjusting elevation, each tile is assigned a land
 * biome. The biomes are assigned in blotches. These tiles can be changed to other biomes (e.g.
 * ocean, lake) later on, and can have the elevation adjusted. This is a very early step in the
 * generation process.
 */
public class BiomePainter implements Generator {

    @Override
    public void generate(World world, Random random) {
        // Compute the biome for each tile. This can be done in parallel.
        world.getTiles().parallelStream().forEach(t -> t.setBiome(computeBiome(t)));
    }

    /**
     * Computes a biome for the tile, as a function of elevation, humidity, and temperature.
     *
     * @param tile the tile
     * @return a biome for the given tile
     */
    private Biome computeBiome(Tile tile) {
        final int elevation = tile.elevation();
        final double humidity = tile.humidity();
        final int temperature = tile.temperature();

        if (elevation > 30) {
            return Biome.MOUNTAIN;
        }

        if (humidity < 0.15) {
            return Biome.DESERT;
        }
        if (humidity < 0.6) {
            return Biome.PLAINS;
        }
        return Biome.FOREST;
    }
}
