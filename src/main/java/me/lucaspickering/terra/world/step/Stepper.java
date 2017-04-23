package me.lucaspickering.terra.world.step;

import java.util.Random;

import me.lucaspickering.terra.world.World;

public abstract class Stepper {

    private final World world;
    private final Random random;

    protected Stepper(World world, Random random) {
        this.world = world;
        this.random = random;
    }

    protected World getWorld() {
        return world;
    }

    protected Random getRandom() {
        return random;
    }

    public abstract void step();

}
