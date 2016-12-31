package me.lucaspickering.terraingen.world;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.BiomePainter;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.LandRougher;
import me.lucaspickering.terraingen.world.generate.OceanFloorGenerator;
import me.lucaspickering.terraingen.world.generate.PeakGenerator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
import me.lucaspickering.terraingen.world.tile.Tile;

public class World {

    // Every tile's elevation must be in this range
    public static final InclusiveRange ELEVATION_RANGE = new InclusiveRange(-50, 75);

    // World size
    private static final int SIZE = 100;

    private static final Generator[] GENERATORS = new Generator[]{
        new BiomePainter(),
        new OceanFloorGenerator(),
        new LandRougher(),
        new PeakGenerator(),
        new WaterPainter(),
        new BeachGenerator()
    };

    private final Logger logger;
    private final Random random;
    private final Tiles tiles;

    // The pixel location of the getCenter of the world
    private Point worldCenter;

    public World() {
        logger = Logger.getLogger(getClass().getName());
        random = TerrainGen.instance().random();
        tiles = genTiles();
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
    }

    private Tiles genTiles() {
        final long startTime = System.currentTimeMillis(); // We're timing this

        final Tiles tiles = new Tiles();
        // Fill out the set with a bunch of points
        for (int x = -SIZE; x <= SIZE; x++) {
            for (int y = -SIZE; y <= SIZE; y++) {
                for (int z = -SIZE; z <= SIZE; z++) {
                    if (x + y + z == 0) {
                        tiles.add(new Tile(new TilePoint(x, y, z)));
                    }
                }
            }
        }

        // Add adjacents for each tile
        for (Tile tile : tiles) {
            // Get all tiles adjacent to this one
            final Map<Direction, Tile> adjacents = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, TilePoint> adjEntry :
                WorldHelper.getAdjacentTiles(tiles, tile.pos()).entrySet()) {

                final Direction dir = adjEntry.getKey();
                final TilePoint adjPoint = adjEntry.getValue();

                // Add the corresponding tile to the map of adjacent tiles
                adjacents.put(dir, tiles.getByPoint(adjPoint));
            }
            tile.setAdjacents(adjacents);
        }

        // Apply each generator in sequence (this is the heavy lifting)
        Arrays.stream(GENERATORS).forEach(gen -> gen.generate(tiles, random));

        final Tiles result = tiles.immutableCopy(); // Make an immutable copy
        logger.log(Level.FINE, String.format("World generation took %d ms",
                                             System.currentTimeMillis() - startTime));
        return result;
    }

    /**
     * Gets the world's tiles. No copy is made, but the returned object is immutable. Its
     * internal objects (tiles, etc.) may be mutable though, so do not change them!
     *
     * @return the world's tiles
     */
    public Tiles getTiles() {
        return tiles;
    }

    public Point getWorldCenter() {
        return worldCenter;
    }

    public void setWorldCenter(Point worldCenter) {
        this.worldCenter = worldCenter;
    }
}
