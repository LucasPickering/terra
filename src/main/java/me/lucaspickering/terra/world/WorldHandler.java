package me.lucaspickering.terra.world;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import me.lucaspickering.terra.world.generate.BeachGenerator;
import me.lucaspickering.terra.world.generate.BiomePainter;
import me.lucaspickering.terra.world.generate.ContinentClusterer;
import me.lucaspickering.terra.world.generate.FreshWaterGenerator;
import me.lucaspickering.terra.world.generate.Generator;
import me.lucaspickering.terra.world.generate.NoiseElevationGenerator;
import me.lucaspickering.terra.world.generate.NoiseHumidityGenerator;
import me.lucaspickering.terra.world.generate.WaterPainter;

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
        final List<Generator> generators = Arrays.stream(Generators.values())
            .map(Generators::makeGenerator)
            .collect(Collectors.toList());

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
}
