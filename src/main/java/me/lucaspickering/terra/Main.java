package me.lucaspickering.terra;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.render.Window;
import me.lucaspickering.terra.render.screen.WorldScreen;
import me.lucaspickering.terra.world.WorldHandler;

public class Main {

    private static final Main MAIN = new Main();

    private final Logger logger;
    private final long seed;
    private final Window window;
    private final Renderer renderer;

    public static void main(String[] args) {
        MAIN.run();
    }

    public static Renderer renderer() {
        return MAIN.renderer;
    }

    private Main() {
        // Basic initialization happens here. GLFW init happens in initWindow()

        // Init logging
        logger = Logger.getLogger(getClass().getName());

        seed = initRandomSeed();
        logger.log(Level.INFO, "Random seed: " + seed);

        window = new Window("Terra", 1280, 720, true);
        renderer = new Renderer();
    }

    private void run() {
        try {
            window.init();
            renderer.init();
            initGame();
            gameLoop(); // Run the game
        } catch (Exception e) {
            handleFatalException(e);
        } finally {
            tearDown(); // Shutdown
        }
    }

    public long initRandomSeed() {
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

    /**
     * Initializes game stuff, i.e. anything that's not OpenGL/GLFW
     */
    private void initGame() {
        final WorldHandler worldHandler = new WorldHandler(seed);
        worldHandler.generate(); // Generate a world
        window.setCurrentScreen(new WorldScreen(worldHandler)); // Initialize the current screen
    }

    private void gameLoop() {
        while (!window.shouldClose()) {
            window.update();
        }
    }

    private void handleFatalException(Exception e) {
        logger.log(Level.SEVERE, "Fatal exception:", e);
    }

    private void tearDown() {
        renderer.cleanup();
        window.cleanup();
    }
}
