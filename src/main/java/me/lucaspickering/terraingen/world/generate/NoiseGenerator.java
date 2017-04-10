package me.lucaspickering.terraingen.world.generate;

import com.flowpowered.noise.module.source.Perlin;

import java.util.Random;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

/**
 * Uses a noise function (Perlin) to generate terrain
 */
public class NoiseGenerator implements Generator {

    private static final double RADIUS = 100;
    private static final Range<Double> NOISE_RANGE = new DoubleRange(0.9, 1.1);

    private Perlin noiseGenerator;

    public NoiseGenerator() {
        noiseGenerator = new Perlin();
        noiseGenerator.setSeed((int) TerrainGen.instance().getSeed());
    }

    @Override
    public void generate(World world, Random random) {
        final TileSet worldTiles = world.getTiles();

        for (Tile tile : worldTiles) {
            final TilePoint pos = tile.pos();
            final double nx = Math.abs(pos.x()) / RADIUS - 0.5;
            final double ny = Math.abs(pos.y()) / RADIUS - 0.5;
            final double nz = Math.abs(pos.z()) / RADIUS - 0.5;

            final double noise = noiseGenerator.getValue(nx, ny, nz);
            System.out.println(noise);
            final int elevation = NOISE_RANGE.mapTo(noise, World.ELEVATION_RANGE);
            tile.setElevation(elevation);
        }
    }
}