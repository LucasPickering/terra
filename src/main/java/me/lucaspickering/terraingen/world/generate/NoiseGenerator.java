package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.module.source.Perlin;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.MathFuncs;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * Uses a noise function (Perlin) to generate terrain
 */
public class NoiseGenerator implements Generator {

    private static final Logger LOGGER = Logger.getLogger(NoiseGenerator.class.getName());

    private Perlin noiseGenerator;

    public NoiseGenerator() {
        noiseGenerator = new Perlin();
        noiseGenerator.setSeed((int) TerrainGen.instance().getSeed());
        noiseGenerator.setFrequency(1.5);
        noiseGenerator.setNoiseQuality(NoiseQuality.BEST);
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        // Get the min and max coordinate values among all tiles (used for generating noise)
        int coordMin = Integer.MAX_VALUE;
        int coordMax = Integer.MIN_VALUE;
        for (Tile tile : worldTiles) {
            final TilePoint pos = tile.pos();
            coordMin = MathFuncs.min(coordMin, pos.x(), pos.y(), pos.z());
            coordMax = MathFuncs.max(coordMax, pos.x(), pos.y(), pos.z());
        }
        final Range<Integer> coordinateRange = new IntRange(coordMin, coordMax);

        // Compute a noise value for each tile. This can be done in parallel.
        final List<Pair<Tile, Double>> noises = worldTiles.parallelStream()
            .map(tile -> generateNoise(tile, coordinateRange))
            .collect(Collectors.toList());

        // Find min and max noise values, and create a range based on them
        double minNoise = Double.MAX_VALUE;
        double maxNoise = Double.MIN_VALUE;
        for (Pair<Tile, Double> pair : noises) {
            final double noise = pair.second();
            minNoise = Math.min(minNoise, noise);
            maxNoise = Math.max(maxNoise, noise);
        }
        final Range<Double> noiseRange = new DoubleRange(minNoise, maxNoise);

        // Map each noise value to an elevation. This can be done in parallel.
        noises.parallelStream().forEach(p -> setElevation(p, noiseRange));

        LOGGER.log(Level.FINER, String.format("Min noise: %f; Max noise: %f",
                                              noiseRange.lower(), noiseRange.upper()));
    }

    private Pair<Tile, Double> generateNoise(Tile tile, Range<Integer> coordinateRange) {
        final TilePoint pos = tile.pos();
        final double nx = coordinateRange.normalize(pos.x());
        final double ny = coordinateRange.normalize(pos.y());
        final double nz = coordinateRange.normalize(pos.z());

        return new Pair<>(tile, noiseGenerator.getValue(nx, ny, nz));
    }

    private void setElevation(Pair<Tile, Double> tileNoise, Range<Double> noiseRange) {
        final Tile tile = tileNoise.first();
        final double noise = tileNoise.second();
        final int elevation = noiseRange.mapTo(noise, World.ELEVATION_RANGE);
        tile.setElevation(elevation);
    }
}
