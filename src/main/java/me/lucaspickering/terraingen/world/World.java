package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.DoubleRange;
import me.lucaspickering.terraingen.util.IntRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.ContinentGenerator;
import me.lucaspickering.terraingen.world.generate.Generator;
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
//        new LandRougher(),
//        new PeakGenerator(),
        new WaterPainter(),
        new BeachGenerator()
    };

    private final Logger logger;
    private final long seed;
    private final int size; // Radius of the world

    // Properties of the world
    private WorldContainer world;

    private Point worldCenter; // The pixel location of the center of the world

    // Tile pixel dimensions
    private double tileRadius;
    private double tileWidth;
    private double tileHeight;
    private Point[] tileVertices;

    public World(long seed) {
        this(seed, DEFAULT_SIZE);
    }

    // Package visible for benchmarking purposes
    World(long seed, int size) {
        this.logger = Logger.getLogger(getClass().getName());
        this.seed = seed;
        this.size = size;

        logger.log(Level.FINE, String.format("Using seed '%d'", seed));
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
        setTileRadius(VALID_TILE_RADII.min());
    }

    /**
     * Generates a new set of tiles to represent this world in parallel with the current thread.
     * The generation process is executed in another thread, so this method will return
     * immediately, before the generation is completed.
     */
    public void generateParallel() {
        // Launch the generation process in a new thread
        new Thread(this::generate).start();
    }

    /**
     * Generates a new set of tiles to represent this world. This method does not return until
     * the generation process is complete.
     */
    public void generate() {
        final long startTime = System.currentTimeMillis(); // We're timing this
        final WorldContainer world = new WorldContainer(size);

        // Apply each generator in sequence (this is the heavy lifting)
        Arrays.stream(GENERATORS).forEach(gen -> runGenerator(gen, world));

        this.world = world.immutableCopy(); // Make an immutable copy and save it for the class
        logger.log(Level.FINE, String.format("World generation took %d ms",
                                             System.currentTimeMillis() - startTime));
    }

    private void runGenerator(Generator generator, WorldContainer world) {
        final long startTime = System.currentTimeMillis();
        final Random random = new Random(seed);
        generator.generate(world, random);
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
        return world.getTiles();
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
        return tileToPixel(tile.pos());
    }

    public final Point getTileTopLeft(Point tileCenter) {
        return tileCenter.plus(-getTileWidth() / 2, -getTileHeight() / 2);
    }

    public Point getTileTopRight(Point tileCenter) {
        return tileCenter.plus(getTileWidth() / 2, -getTileHeight() / 2);
    }

    public Point getTileBottomRight(Point tileCenter) {
        return tileCenter.plus(getTileWidth() / 2, getTileHeight() / 2);
    }

    public Point getTileBottomLeft(Point tileCenter) {
        return tileCenter.plus(-getTileWidth() / 2, getTileHeight() / 2);
    }


    /**
     * Converts a {@link TilePoint} in this world to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link TilePoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public Point tileToPixel(@NotNull TilePoint tile) {
        final double x = getTileWidth() * tile.x() * 0.75f;
        final double y = -getTileHeight() * (tile.x() / 2.0f + tile.y());
        return getWorldCenter().plus((int) x, (int) y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link TilePoint} in this world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in this world; it is just the position of a theoretical tile that could
     * exist there. The given point does not need to be shifted based on the world center before
     * calling this function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public TilePoint pixelToTile(@NotNull Point pos) {
        final Point shiftedPos = pos.minus(getWorldCenter());
        // Convert it to a fractional tile point
        final double fracX = shiftedPos.x() * 4f / 3f / getTileWidth();
        final double fracY = -(shiftedPos.x() + Math.sqrt(3) * shiftedPos.y())
                             / (getTileRadius() * 3f);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return TilePoint.roundPoint(fracX, fracY, fracZ);
    }
}
