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
import me.lucaspickering.terra.world.generate.Generator;
import me.lucaspickering.terra.world.generate.NoiseElevationGenerator;
import me.lucaspickering.terra.world.generate.NoiseHumidityGenerator;
import me.lucaspickering.terra.world.generate.OceanGenerator;
import me.lucaspickering.terra.world.generate.RunoffGenerator;
import me.lucaspickering.terra.world.step.FreshWaterStepper;
import me.lucaspickering.terra.world.step.Stepper;

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
        OCEAN_GENERATOR(OceanGenerator.class),
        BIOME_PAINTER(BiomePainter.class),
        CONTINENT_CLUSTERER(ContinentClusterer.class),
        BEACH_GENERATOR(BeachGenerator.class),
        RUNOFF_GENERATOR(RunoffGenerator.class);

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

    private World world;
    private Random random;
    private Stepper stepper;

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
     * Generates a new set of tiles to represent this world. This method does not return until
     * the generation process is complete.
     */
    public void generate() {
        random = new Random(seed); // Init the Random instance

        // Initialize generators outside the timer. This may get its own timer later?
        final List<Generator> generators = Arrays.stream(Generators.values())
            .map(Generators::makeGenerator)
            .collect(Collectors.toList());

        final long startTime = System.currentTimeMillis(); // We're timing this
        final World world = new World(seed, size);

        // Apply each generator in sequence (this is the heavy lifting)
        for (Generator generator : generators) {
            runGenerator(generator, world);
        }

        this.world = world.immutableCopy(); // Make an immutable copy and save it for the class
        final long elapsedTime = System.currentTimeMillis() - startTime; // Stop the timer
        logger.log(Level.INFO, String.format("World generation took %d ms", elapsedTime));

        stepper = new FreshWaterStepper(this.world, random); // TODO remove placeholder
    }

    private void runGenerator(Generator generator, World world) {
        final long startTime = System.currentTimeMillis();
        generator.generate(world, random);
        final long runTime = System.currentTimeMillis() - startTime;
        logger.log(Level.FINE, String.format("Generator stage %s took %d ms",
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

    /**
     * Advances the current {@link Stepper} by one step.
     *
     * @throws NullPointerException if there is no active {@link Stepper}
     */
    public void step() {
        stepper.step();
    }
}
