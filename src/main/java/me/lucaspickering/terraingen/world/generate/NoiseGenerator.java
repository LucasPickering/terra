package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
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
        noiseGenerator.setFrequency(3.5);
        noiseGenerator.setLacunarity(Perlin.DEFAULT_PERLIN_LACUNARITY);
        noiseGenerator.setPersistence(Perlin.DEFAULT_PERLIN_PERSISTENCE);
        noiseGenerator.setOctaveCount(12);
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        // Build a range that bounds all x, y, and z values
        final List<Integer> coords = new ArrayList<>(worldTiles.size() * 3);
        for (Tile tile : worldTiles) {
            final TilePoint pos = tile.pos();
            coords.add(pos.x());
            coords.add(pos.y());
            coords.add(pos.z());
        }
        final Range<Integer> coordinateRange = new IntRange(coords);

        // Compute a noise value for each tile. This can be done in parallel.
        final Map<Tile, Double> noises = worldTiles.parallelStream()
            .map(tile -> generateNoise(tile, coordinateRange))
            .collect(Pair.mapCollector());

        // Create a range that bounds the noises
        final Range<Double> noiseRange = new DoubleRange(noises.values());

        // Map each noise value to an elevation. This can be done in parallel.
        noises.entrySet().parallelStream().forEach(e -> setElevation(e.getKey(),
                                                                     e.getValue(),
                                                                     noiseRange));

        LOGGER.log(Level.FINER, String.format("Noise range: %s", noiseRange));
    }

    private Pair<Tile, Double> generateNoise(Tile tile, Range<Integer> coordinateRange) {
        final TilePoint pos = tile.pos();
        final double nx = coordinateRange.normalize(pos.x());
        final double ny = coordinateRange.normalize(pos.y());
        final double nz = coordinateRange.normalize(pos.z());

        return new Pair<>(tile, noiseGenerator.getValue(nx, ny, nz));
    }

    private void setElevation(Tile tile, double noise, Range<Double> noiseRange) {
        final int elevation = noiseRange.mapTo(noise, World.ELEVATION_RANGE);
        tile.setElevation(elevation);
    }
}
