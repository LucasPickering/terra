package me.lucaspickering.terra.world;

import java.util.Random;
import java.util.logging.Logger;

import me.lucaspickering.terra.world.generate.*;
import me.lucaspickering.utils.GeneralFuncs;

/**
 * A class with fields and methods that can entirely encapsulate a {@link World} and perform useful
 * operations on it. This handles functionality such as generating worlds and finding tile pixel
 * locations & sizes.
 *
 * This should be a singleton class.
 */
public class WorldHandler {

    private static final int DEFAULT_CHUNK_RADIUS = 3; // Default radius of the world, in chunks

    private final Logger logger;
    private final long seed;
    private final int size; // Radius of the world

    private World world;
    private Random random;

    public WorldHandler(long seed) {
        this(seed, DEFAULT_CHUNK_RADIUS);
    }

    // Package visible for benchmarking purposes
    WorldHandler(long seed, int size) {
        this.logger = Logger.getLogger(getClass().getName());
        this.seed = seed;
        this.size = size;
    }

    /**
     * Generates a new set of tiles to represent this world. This method does not return until the
     * generation process is complete.
     */
    public void generate() {
        random = new Random(seed); // Init the Random instance

        // Generate the world, and time how long it takes
        final long time = GeneralFuncs.timed(() -> {
            final World world = new World(seed, size);
            final Generator[] generators = makeGenerators(world, random); // Initialize generators

            // Apply each generator in sequence (this is the heavy lifting)
            for (Generator generator : generators) {
                runGenerator(generator);
            }

            this.world = world.immutableCopy(); // Make an immutable copy and save it for the class
        });
        logger.info(String.format("Generated %d chunks, %s tiles in %d ms",
                                  world.getChunks().size(), world.getTiles().size(), time));
    }

    private Generator[] makeGenerators(World world, Random random) {
        return new Generator[]{
            new NoiseElevationGenerator(world, random),
            new NoiseHumidityGenerator(world, random),
            new OceanGenerator(world, random),
            new BiomePainter(world, random),
            new ContinentClusterer(world, random),
            new BeachGenerator(world, random),
            new RunoffGenerator(world, random),
//            new LakeGenerator(world, random),
            new RiverGenerator(world, random)
        };
    }

    private void runGenerator(Generator generator) {
        final long time = GeneralFuncs.timed(generator::generate);
        logger.fine(String.format("Generator stage %s took %d ms",
                                  generator.getClass().getSimpleName(), time));
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

}
