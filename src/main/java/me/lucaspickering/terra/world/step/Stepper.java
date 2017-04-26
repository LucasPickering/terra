package me.lucaspickering.terra.world.step;

import java.util.Random;
import java.util.logging.Logger;

import me.lucaspickering.terra.world.World;

public abstract class Stepper {

    private final Logger logger;
    private final World world;
    private final Random random;

    protected Stepper(World world, Random random) {
        logger = Logger.getLogger(getClass().getName());
        this.world = world;
        this.random = random;
    }

    protected Logger logger() {
        return logger;
    }

    protected World world() {
        return world;
    }

    protected Random random() {
        return random;
    }

    public abstract void step();

}
