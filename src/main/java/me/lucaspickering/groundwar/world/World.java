package me.lucaspickering.groundwar.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.groundwar.TerrainGen;
import me.lucaspickering.groundwar.render.Renderer;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.generate.BiomeGenerator;
import me.lucaspickering.groundwar.world.generate.Generator;
import me.lucaspickering.groundwar.world.generate.PeakGenerator;
import me.lucaspickering.groundwar.world.tile.Tile;

public class World {

    /**
     * The on-screen location of the center of the world
     */
    public static final Point WORLD_CENTER = new Point(Renderer.RES_WIDTH / 2,
                                                       Renderer.RES_HEIGHT / 2);
    public static final int MAX_ELEVATION = 100;

    // Board size
    private static final int X_SIZE = 5, Y_SIZE = 5, Z_SIZE = 5;

    private static final Generator[] GENERATORS = new Generator[]{
        new PeakGenerator(),
        new BiomeGenerator()
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
        for (int x = -X_SIZE; x <= X_SIZE; x++) {
            for (int y = -Y_SIZE; y <= Y_SIZE; y++) {
                for (int z = -Z_SIZE; z <= Z_SIZE; z++) {
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
