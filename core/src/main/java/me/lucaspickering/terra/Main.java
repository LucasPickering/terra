package me.lucaspickering.terra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import java.util.Random;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.InputHandler;
import me.lucaspickering.terra.render.screen.ScreenHandler;
import me.lucaspickering.terra.render.screen.WorldScreen;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.world.WorldHandler;

public class Main extends ApplicationAdapter {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final ScreenHandler screenHandler = new ScreenHandler();

    private long seed;

    @Override
    public void create() {
        // Init random seed
        seed = initRandomSeed();
        logger.info("Random seed: " + seed);

        initRendering();
        initGame();

        // Create and register our input handler
        Gdx.input.setInputProcessor(new InputHandler(screenHandler));
    }

    private long initRandomSeed() {
        // Set random seed
        final String seedString = System.getProperty("seed");
        // If a seed was provided, use that, otherwise generate our own seed
        if (seedString != null) {
            // Try to parse the seed as a Long, and if that fails, just hash the string
            try {
                return Long.parseLong(seedString);
            } catch (NumberFormatException e) {
                return seedString.hashCode();
            }
        } else {
            // Generate a seed and record it, so that it can be logged
            // Maybe we could just use system time here? This seems more xD random though
            return new Random().nextLong();
        }
    }

    private void initRendering() {
        // Set clear color
        Gdx.gl.glClearColor(Colors.CLEAR.r, Colors.CLEAR.g, Colors.CLEAR.b, Colors.CLEAR.a);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void initGame() {
        final WorldHandler worldHandler = new WorldHandler(seed);
        worldHandler.generate(); // Generate a world
        screenHandler.setCurrentScreen(new WorldScreen(worldHandler)); // Create initial screen
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        screenHandler.getCurrentScreen().draw(null);
    }

    @Override
    public void dispose() {
        screenHandler.getCurrentScreen().dispose();
    }
}
