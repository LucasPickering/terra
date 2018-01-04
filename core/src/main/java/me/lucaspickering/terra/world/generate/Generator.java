package me.lucaspickering.terra.world.generate;

import java.util.Random;
import java.util.logging.Logger;

import me.lucaspickering.terra.world.World;

/**
 * Represents one stage in the world generation process. A {@link Generator} is constructed with a
 * {@link World} and an {@link Random}. When {@link #generate()} is called, the {@link World}
 * instance will be modified.
 *
 * A specific instance of this type should only be used once. If you want to generate a new world,
 * create a new instance of each generator you want to use to ensure that no state is left over from
 * the previous generation.
 */
public abstract class Generator {

    private final World world;
    private final Random random;
    private final Logger logger;

    protected Generator(World world, Random random) {
        this.world = world;
        this.random = random;
        logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Modifies the given world, generating new terrain features.
     */
    public abstract void generate();

    protected final World world() {
        return world;
    }

    protected final Random random() {
        return random;
    }

    protected final Logger logger() {
        return logger;
    }
}
