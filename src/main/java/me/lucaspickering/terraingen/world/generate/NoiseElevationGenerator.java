package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

/**
 * Uses a noise function (Perlin) to generate elevation and humidity values for each tile.
 */
public class NoiseElevationGenerator extends NoiseGenerator {

    private static final Logger LOGGER = Logger.getLogger(NoiseElevationGenerator.class.getName());

    public NoiseElevationGenerator() {
        super(new Perlin());
        noiseGenerator.setSeed((int) TerrainGen.instance().getSeed());
        noiseGenerator.setFrequency(3.5);
        noiseGenerator.setLacunarity(Perlin.DEFAULT_PERLIN_LACUNARITY);
        noiseGenerator.setPersistence(Perlin.DEFAULT_PERLIN_PERSISTENCE);
        noiseGenerator.setOctaveCount(12);
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        final Map<Tile, Double> noises = super.generateNoises(worldTiles);
        final Range<Double> noiseRange = new DoubleRange(noises.values());

        // Map each noise value to an elevation. This can be done in parallel.
        noises.entrySet().parallelStream().forEach(e -> setElevation(e.getKey(),
                                                                     e.getValue(),
                                                                     noiseRange));

        LOGGER.log(Level.FINEST, String.format("Noise range: %s", noiseRange));
    }

    private void setElevation(Tile tile, double noise, Range<Double> noiseRange) {
        final int elevation = noiseRange.mapTo(noise, World.ELEVATION_RANGE);
        tile.setElevation(elevation);
    }
}
