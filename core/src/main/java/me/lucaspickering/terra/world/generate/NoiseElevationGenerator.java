package me.lucaspickering.terra.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

/**
 * Uses a noise function (Perlin) to generate elevation and humidity values for each tile.
 */
public class NoiseElevationGenerator extends NoiseGenerator {

    public NoiseElevationGenerator(World world, Random random) {
        super(world, random, new Perlin());
        noiseGenerator.setFrequency(1.5);
        noiseGenerator.setLacunarity(4);
        noiseGenerator.setPersistence(0.5);
        noiseGenerator.setOctaveCount(12);
    }

    @Override
    public void generate() {
        noiseGenerator.setSeed((int) world().getSeed());
        final TileSet worldTiles = world().getTiles();

        final Map<Tile, Double> noises = super.generateNoises(worldTiles);
        final Range<Double> noiseRange = new DoubleRange(noises.values());

        // Map each noise value to an elevation. This can be done in parallel.
        noises.entrySet().parallelStream().forEach(e -> setElevation(e.getKey(),
                                                                     e.getValue(),
                                                                     noiseRange));

        logger().finer(String.format("Noise range: %s", noiseRange));
    }

    private void setElevation(Tile tile, double noise, Range<Double> noiseRange) {
        final double elevation = noiseRange.mapTo(noise, World.ELEVATION_RANGE);
        tile.setElevation(elevation);
    }
}
