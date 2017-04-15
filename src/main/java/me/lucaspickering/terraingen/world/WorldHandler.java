package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.BiomePainter;
import me.lucaspickering.terraingen.world.generate.ContinentClusterer;
import me.lucaspickering.terraingen.world.generate.FreshWaterGenerator;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.NoiseElevationGenerator;
import me.lucaspickering.terraingen.world.generate.NoiseHumidityGenerator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.utils.Point;

/**
 * A class with fields and methods that can entirely encapsulate a {@link World} and
 * perform useful operations on it. This handles functionality such as generating worlds and
 * finding tile pixel locations & sizes.
 *
 * This should be a singleton class.
 */
public class WorldHandler {

    private enum Generators {

        ELEV_GENERATOR(NoiseElevationGenerator.class),
        HUMID_GENERATOR(NoiseHumidityGenerator.class),
        WATER_PAINTER(WaterPainter.class),
        BIOME_PAINTER(BiomePainter.class),
        CONTINENT_CLUSTERER(ContinentClusterer.class),
        BEACH_GENERATOR(BeachGenerator.class),
        FRESH_WATER_GENERATOR(FreshWaterGenerator.class);

        private final Class<? extends Generator> clazz;

        Generators(Class<? extends Generator> clazz) {
            this.clazz = clazz;
        }

        private Generator makeGenerator() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error instantiating generator");
            }
        }
    }

    /**
     * The zoom factor on the world must in this range
     */

    public static final double TILE_WIDTH = 20.0;
    public static final double TILE_HEIGHT = (float) Math.sqrt(3) * TILE_WIDTH / 2.0;
    public static final Point[] TILE_VERTICES = {
        new Point(-TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-left
        new Point(+TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-right
        new Point(+TILE_WIDTH / 2, 0),                // Right
        new Point(+TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-right
        new Point(-TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-left
        new Point(-TILE_WIDTH / 2, 0)                 // Left
    };

    private static final int DEFAULT_CHUNK_RADIUS = 3; // Default radius of the world, in chunks

    private final Logger logger;
    private final long seed;
    private final int size; // Radius of the world

    // Properties of the world
    private World world;

    public WorldHandler(long seed) {
        this(seed, DEFAULT_CHUNK_RADIUS);
    }

    // Package visible for benchmarking purposes
    WorldHandler(long seed, int size) {
        this.logger = Logger.getLogger(getClass().getName());
        this.seed = seed;
        this.size = size;

        logger.log(Level.FINE, String.format("Using seed '%d'", seed));

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
        // Initialize generators outside the timer. This may get its own timer later?
        final List<Generator> generators = Arrays.stream(Generators.values()).map
            (Generators::makeGenerator).collect(Collectors.toList());

        final long startTime = System.currentTimeMillis(); // We're timing this
        final World world = new World(size);

        // Apply each generator in sequence (this is the heavy lifting)
        for (Generator generator : generators) {
            runGenerator(generator, world);
        }

        this.world = world.immutableCopy(); // Make an immutable copy and save it for the class
        logger.log(Level.FINE, String.format("World generation took %d ms",
                                             System.currentTimeMillis() - startTime));
    }

    private void runGenerator(Generator generator, World world) {
        final long startTime = System.currentTimeMillis();
        final Random random = new Random(seed);
        generator.generate(world, random);
        final long runTime = System.currentTimeMillis() - startTime;
        logger.log(Level.FINER, String.format("Generator stage %s took %d ms",
                                              generator.getClass().getSimpleName(), runTime));
    }

    /**
     * Gets the current world for this handler. No copy is made, but the returned object is
     * immutable.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    public Point getTileCenter(Tile tile) {
        return tileToPixel(tile.pos());
    }

    /**
     * Converts a {@link HexPoint} in this world to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public Point tileToPixel(@NotNull TilePoint tile) {
        final double x = TILE_WIDTH * tile.x() * 0.75;
        final double y = -TILE_HEIGHT * (tile.x() / 2.0 + tile.y());
        return new Point(x, y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link HexPoint} in this world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in this world; it is just the position of a theoretical tile that could
     * exist there. The given point should be shifted so that it is relative to the origin rather
     * than the center of the screen.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public HexPoint pixelToTile(@NotNull Point pos) {
        // Convert it to a fractional tile point
        final double fracX = pos.x() * 4.0 / 3.0 / TILE_WIDTH;
        final double fracY = -(pos.x() + Math.sqrt(3.0) * pos.y())
                             / (TILE_WIDTH * 1.5);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return HexPoint.roundPoint(fracX, fracY, fracZ);
    }
}
