package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    Range<Integer> getCoordinateRange(TileSet tiles) {
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
        return generateNoise(tile,
                             t -> coordinateRange.normalize(t.pos().x()),
                             t -> coordinateRange.normalize(t.pos().y()),
                             t -> coordinateRange.normalize(t.pos().z()));
    }

    Pair<Tile, Double> generateNoise(Tile tile,
                                     Function<Tile, Double> xFunc,
                                     Function<Tile, Double> yFunc,
                                     Function<Tile, Double> zFunc) {
        final double x = xFunc.apply(tile);
        final double y = yFunc.apply(tile);
        final double z = zFunc.apply(tile);

        return new Pair<>(tile, noiseGenerator.getValue(x, y, z));
    }
}
