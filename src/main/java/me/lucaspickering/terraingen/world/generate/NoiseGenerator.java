package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Map;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.Pair;

/**
 * A generator that uses a noise function to generate some type of values.
 */
abstract class NoiseGenerator implements Generator {

    private static final double VALUE_RANGE = 128.0;

    final Perlin noiseGenerator;

    public NoiseGenerator(Perlin noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Generates a noise value for each tile and returns all values in a map.
     *
     * @param tiles the set of tiles to generate noises for
     * @return a map containing tile:noise entries, with exactly one entry for each tile in the
     * given set.
     */
    Map<Tile, Double> generateNoises(TileSet tiles) {
        // Compute a noise value for each tile. This can be done in parallel, as the calculations
        // are all independent of each other.
        return tiles.parallelStream()
            .map(t -> new Pair<>(t, generateNoise(t))) // Generate a noise for each tile
            .collect(Pair.mapCollector()); // Collect the tile:noise pairs into a map
    }

    /**
     * Generates a noise value for the given tile. This value is entirely independent of all
     * other tiles in the world, and is guaranteed to be the same on subsequent calls with the
     * same input, as long as the settings of the noise generator don't change.
     *
     * @param tile the tile to generate noise for
     * @return the noise value for the given tile
     */
    private double generateNoise(Tile tile) {
        final TilePoint pos = tile.pos();

        // The Perlin noise function relies of receiving non-integer input in order to generate
        // reasonable noise values. Divide x/y/z by some large constant to get decimal values.
        final double nx = pos.x() / VALUE_RANGE;
        final double ny = pos.y() / VALUE_RANGE;
        final double nz = pos.z() / VALUE_RANGE;

        return noiseGenerator.getValue(nx, ny, nz);
    }
}
