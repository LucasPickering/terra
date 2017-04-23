package me.lucaspickering.terra.world.generate;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;

import me.lucaspickering.terra.world.Biome;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;

/**
 * Paints biomes onto each continent. Without adjusting elevation, each tile is assigned a land
 * biome. The biomes are assigned in blotches. These tiles can be changed to other biomes (e.g.
 * ocean, lake) later on, and can have the elevation adjusted. This is a very early step in the
 * generation process.
 */
public class BiomePainter implements Generator {

    // Basically a typedef, for convenience
    private interface BiomeFunction extends BiPredicate<Integer, Double> {

    }

    // These functions represent a 2D graph of humidity:elevation, where there is a region of the
    // graph designated for each biome. Each function defines a region boundary. The ordering of
    // biomes in the enum class define which biome gets priority in the overlapping cases.
    private static Map<Biome, BiomeFunction> biomeFuncs =
        new EnumMap<Biome, BiomeFunction>(Biome.class) {{
            // I swear there's logic behind these I even drew a picture
            put(Biome.SNOW, (e, h) -> e >= -5 * h + 35);
            put(Biome.DESERT, (e, h) -> h <= 0.15);
            put(Biome.ALPINE, (e, h) -> e >= -5 * h + 20);
            put(Biome.JUNGLE, (e, h) -> h >= 0.75);
            put(Biome.FOREST, (e, h) -> e >= -44 * h + 27);
            put(Biome.PLAINS, (e, h) -> true); // Default case
        }};

    @Override

    public void generate(World world, Random random) {
        // Compute the biome for each tile. This can be done in parallel.
        world.getTiles().parallelStream()
            .filter(t -> !t.biome().isWater()) // Don't re-compute for water tiles
            .forEach(t -> t.setBiome(computeBiome(t)));
    }

    /**
     * Computes a biome for the tile, as a function of elevation and humidity.
     *
     * @param tile the tile
     * @return a biome for the given tile
     */
    private Biome computeBiome(Tile tile) {
        final int elevation = tile.elevation();
        final double humidity = tile.humidity();

        // Test the function for each biome. As soon as one returns true, use that biome.
        for (Map.Entry<Biome, BiomeFunction> entry : biomeFuncs.entrySet()) {
            final Biome biome = entry.getKey();
            final BiomeFunction func = entry.getValue();
            if (func.test(elevation, humidity)) {
                return biome;
            }
        }

        throw new IllegalStateException(String.format(
            "No biome found for [elevation=%d], [humidity=%f]", elevation, humidity));
    }
}
