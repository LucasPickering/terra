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
 * Uses a noise function to generate humidity values for each tile.
 */
public class NoiseHumidityGenerator extends NoiseGenerator {

    private static final Logger LOGGER = Logger.getLogger(NoiseHumidityGenerator.class.getName());

    public NoiseHumidityGenerator() {
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
        noises.entrySet().parallelStream().forEach(e -> setHumidity(e.getKey(),
                                                                    e.getValue(),
                                                                    noiseRange));

        LOGGER.log(Level.FINEST, String.format("Noise range: %s", noiseRange));
    }

    private void setHumidity(Tile tile, double noise, Range<Double> noiseRange) {
        final double humidity = noiseRange.mapTo(noise, World.HUMIDITY_RANGE);
        tile.setHumidity(humidity);
    }
}
