package me.lucaspickering.terra.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.Main;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

/**
 * Uses a noise function to generate humidity values for each tile.
 */
public class NoiseHumidityGenerator extends NoiseGenerator {

    private static final Logger LOGGER = Logger.getLogger(NoiseHumidityGenerator.class.getName());

    public NoiseHumidityGenerator() {
        super(new Perlin());
        final long seed = Main.instance().getSeed();
        noiseGenerator.setSeed((int) (seed * seed)); // Square the seed to vary it
        noiseGenerator.setFrequency(8.0);
        noiseGenerator.setLacunarity(Perlin.DEFAULT_PERLIN_LACUNARITY);
        noiseGenerator.setPersistence(Perlin.DEFAULT_PERLIN_PERSISTENCE);
        noiseGenerator.setOctaveCount(Perlin.DEFAULT_PERLIN_OCTAVE_COUNT);
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        final Map<Tile, Double> noises = super.generateNoises(worldTiles);
        final Range<Double> noiseRange = new DoubleRange(noises.values());

        // Map each noise value to a humidity. This can be done in parallel.
        noises.entrySet().parallelStream().forEach(e -> setHumidity(e.getKey(),
                                                                    e.getValue(),
                                                                    noiseRange));

        LOGGER.log(Level.FINER, String.format("Noise range: %s", noiseRange));
    }

    private void setHumidity(Tile tile, double noise, Range<Double> noiseRange) {
        final double humidity = noiseRange.mapTo(noise, World.HUMIDITY_RANGE);
        tile.setHumidity(humidity);
    }
}
