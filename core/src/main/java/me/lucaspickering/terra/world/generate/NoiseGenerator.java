package me.lucaspickering.terra.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Random;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.terra.world.util.TileSet;
import me.lucaspickering.utils.Pair;

/**
 * A generator that uses a noise function to generate some type of values.
 */
abstract class NoiseGenerator extends Generator {

    private static final double VALUE_RANGE = 256.0;

    protected final Perlin noiseGenerator;

    public NoiseGenerator(World world, Random random, Perlin noiseGenerator) {
        super(world, random);
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Generates a noise value for each tile and returns all values in a map.
     *
     * @param tiles the set of tiles to generate noises for
     * @return a map containing tile:noise entries, with exactly one entry for each tile in the
     * given set.
     */
    HexPointMap<Tile, Double> generateNoises(TileSet tiles) {
        // Compute a noise value for each tile. This can be done in parallel, but it seems to
        // actually take longer that way so let's not.
        return tiles.stream()
            .map(t -> new Pair<>(t, generateNoise(t))) // Generate a noise for each tile
            .collect(Pair.mapCollector(HexPointMap::new));
    }

    /**
     * Generates a noise value for the given tile. This value is entirely independent of all other
     * tiles in the world, and is guaranteed to be the same on subsequent calls with the same input,
     * as long as the settings of the noise generator don't change.
     *
     * @param tile the tile to generate noise for
     * @return the noise value for the given tile
     */
    private double generateNoise(Tile tile) {
        final HexPoint pos = tile.pos();

        // The Perlin noise function relies of receiving non-integer input in order to generate
        // reasonable noise values. Divide x/y/z by some large constant to get decimal values.
        final double nx = pos.x() / VALUE_RANGE;
        final double ny = pos.y() / VALUE_RANGE;
        final double nz = pos.z() / VALUE_RANGE;

        return noiseGenerator.getValue(nx, ny, nz);
    }
}
