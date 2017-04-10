package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.Pair;
import me.lucaspickering.utils.range.IntRange;
import me.lucaspickering.utils.range.Range;

/**
 * A generator that uses a noise function to generate some type of values.
 */
abstract class NoiseGenerator implements Generator {

    final Perlin noiseGenerator;

    public NoiseGenerator(Perlin noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
    }

    Map<Tile, Double> generateNoises(TileSet tiles) {
        // Build a range that bounds all x, y, and z values
        final List<Integer> coords = new ArrayList<>(tiles.size() * 3);
        for (Tile tile : tiles) {
            final TilePoint pos = tile.pos();
            coords.add(pos.x());
            coords.add(pos.y());
            coords.add(pos.z());
        }
        final Range<Integer> coordinateRange = new IntRange(coords);

        // Compute a noise value for each tile. This can be done in parallel.
        return tiles.parallelStream()
            .map(tile -> generateNoise(tile, coordinateRange))
            .collect(Pair.mapCollector());
    }

    private Range<Integer> getCoordinateRange(TileSet tiles) {
        final List<Integer> coords = new ArrayList<>(tiles.size() * 3);
        for (Tile tile : tiles) {
            final TilePoint pos = tile.pos();
            coords.add(pos.x());
            coords.add(pos.y());
            coords.add(pos.z());
        }
        return new IntRange(coords);
    }

    private Pair<Tile, Double> generateNoise(Tile tile, Range<Integer> coordinateRange) {
        final TilePoint pos = tile.pos();
        final double nx = coordinateRange.normalize(pos.x());
        final double ny = coordinateRange.normalize(pos.y());
        final double nz = coordinateRange.normalize(pos.z());

        return new Pair<>(tile, noiseGenerator.getValue(nx, ny, nz));
    }
}
