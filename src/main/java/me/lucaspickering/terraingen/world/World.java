package me.lucaspickering.terraingen.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.BiomeGenerator;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.OceanGenerator;
import me.lucaspickering.terraingen.world.generate.PeakGenerator;
import me.lucaspickering.terraingen.world.tile.Tile;

public class World {

    /**
     * The on-screen location of the center of the world
     */
    public static final Point WORLD_CENTER = new Point(Renderer.RES_WIDTH / 2,
                                                       Renderer.RES_HEIGHT / 2);

    // Every tile's elevation must be in one of these two ranges
    public static final InclusiveRange LOWER_ELEVATION_RANGE = new InclusiveRange(-50, 0);
    public static final InclusiveRange UPPER_ELEVATION_RANGE = new InclusiveRange(0, 100);

    // World size
    private static final int SIZE = 20;

    private static final Generator[] GENERATORS = new Generator[]{
        new OceanGenerator(),
        new PeakGenerator(),
        new BiomeGenerator(),
        new BeachGenerator()
        };

    private final Random random;
    private final Map<TilePoint, Tile> tiles;

    public World() {
        random = TerrainGen.instance().random();
        tiles = genTiles();
    }

    private Map<TilePoint, Tile> genTiles() {
        final Set<TilePoint> points = new HashSet<>();
        // Fill out the set with a bunch of points
        for (int x = -SIZE; x <= SIZE; x++) {
            for (int y = -SIZE; y <= SIZE; y++) {
                for (int z = -SIZE; z <= SIZE; z++) {
                    if (x + y + z == 0) {
                        points.add(new TilePoint(x, y, z));
                    }
                }
            }
        }

        final WorldBuilder worldBuilder = new WorldBuilder(points);

        // Apply each generator in sequence
        Arrays.stream(GENERATORS).forEach(gen -> gen.generate(worldBuilder, random));

        // Build the world into a map of point:tile and return it
        return worldBuilder.build();
    }

    /**
     * Gets the world's copy of tiles. This is NOT a copy, so DO NOT MODIFY IT.
     *
     * @return the world's copy of tiles
     */
    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }
}
