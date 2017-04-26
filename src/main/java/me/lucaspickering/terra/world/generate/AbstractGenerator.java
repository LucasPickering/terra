package me.lucaspickering.terra.world.generate;

import java.util.logging.Logger;

abstract class AbstractGenerator implements Generator {

    private final Logger logger;

    protected AbstractGenerator() {
        logger = Logger.getLogger(getClass().getName());
    }

    protected Logger logger() {
        return logger;
    }
}
