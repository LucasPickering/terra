package me.lucaspickering.terraingen.world;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.DoubleRange;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.ContinentGenerator;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.LandRougher;
import me.lucaspickering.terraingen.world.generate.PeakGenerator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
import me.lucaspickering.terraingen.world.tile.Tile;

public class World {

    // Every tile's elevation must be in this range
    public static final IntRange ELEVATION_RANGE = new IntRange(-25, 25);

    public static final DoubleRange VALID_TILE_RADII = new DoubleRange(10, 200);

    /**
     * Any tile below, but not equal to, this elevation can feasibly become ocean tiles. Most
     * land tiles will be at or above this elevation.
     */
    public static final int SEA_LEVEL = 0;

    // World size
    private static final int DEFAULT_SIZE = 100;

    private static final Generator[] GENERATORS = new Generator[]{
        new ContinentGenerator(),
        new LandRougher(),
        new PeakGenerator(),
        new WaterPainter(),
        new BeachGenerator()
    };

    private final Logger logger;
    private final Random random;
    private final Tiles tiles;

    // The pixel location of the center of the world
    private Point worldCenter;

    // Tile pixel dimensions
    private double tileRadius;
    private double tileWidth;
    private double tileHeight;
    private Point[] tileVertices;

    public World() {
        this(DEFAULT_SIZE);
    }

    // Package visible for benchmarking purposes
    World(int size) {
        logger = Logger.getLogger(getClass().getName());
        random = TerrainGen.instance().random();
        tiles = generateWorld(size);
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
        setTileRadius(VALID_TILE_RADII.min());
    }

    private Tiles generateWorld(int size) {
        final long startTime = System.currentTimeMillis(); // We're timing this
        final Tiles tiles = WorldHelper.initTiles(size);

        // Apply each generator in sequence (this is the heavy lifting)
        Arrays.stream(GENERATORS).forEach(gen -> runGenerator(gen, tiles, random));

        final Tiles result = tiles.immutableCopy(); // Make an immutable copy
        logger.log(Level.FINE, String.format("World generation took %d ms",
                                             System.currentTimeMillis() - startTime));
        return result;
    }

    private void runGenerator(Generator generator, Tiles tiles, Random random) {
        final long startTime = System.currentTimeMillis();
        generator.generate(tiles, random);
        final long runTime = System.currentTimeMillis() - startTime;
        logger.log(Level.FINER, String.format("Generator stage %s took %d ms",
                                              generator.getClass().getSimpleName(), runTime));
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

    public double getTileRadius() {
        return tileRadius;
    }

    public void setTileRadius(double radius) {
        tileRadius = VALID_TILE_RADII.coerce(radius);
        tileWidth = tileRadius * 2;
        tileHeight = (int) (Math.sqrt(3) * tileRadius);
        tileVertices = new Point[]{
            new Point(-tileWidth / 4, -tileHeight / 2),
            new Point(tileWidth / 4, -tileHeight / 2),
            new Point(tileRadius, 0),
            new Point(tileWidth / 4, tileHeight / 2),
            new Point(-tileWidth / 4, tileHeight / 2),
            new Point(-tileRadius, 0)
        };
    }

    public double getTileWidth() {
        return tileWidth;
    }

    public double getTileHeight() {
        return tileHeight;
    }

    public Point[] getTileVertices() {
        return tileVertices;
    }

    public final Point getTileCenter(Tile tile) {
        return WorldHelper.tileToPixel(this, tile.pos());
    }

    public final Point getTileTopLeft(Tile tile) {
        return getTileCenter(tile).plus(-getTileWidth() / 2, -getTileHeight() / 2);
    }

    public Point getTileTopRight(Tile tile) {
        return getTileCenter(tile).plus(getTileWidth() / 2, -getTileHeight() / 2);
    }

    public Point getTileBottomRight(Tile tile) {
        return getTileCenter(tile).plus(getTileWidth() / 2, getTileHeight() / 2);
    }

    public Point getTileBottomLeft(Tile tile) {
        return getTileCenter(tile).plus(-getTileWidth() / 2, getTileHeight() / 2);
    }
}
