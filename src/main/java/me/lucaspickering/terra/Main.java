package me.lucaspickering.terra;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.InputHandler;
import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.render.screen.Screen;
import me.lucaspickering.terra.render.screen.WorldScreen;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.world.WorldHandler;
import me.lucaspickering.utils.Point;

public class Main {

    private static final Main MAIN = new Main();

    private final Logger logger;
    private final long seed;
    private boolean debug; // True if we are in debug mode

    private final InputHandler inputHandler;

    private long window;
    private Renderer renderer;
    private Screen currentScreen;
    private int windowWidth;
    private int windowHeight;
    private double lastFpsUpdate; // Time of the last FPS update, in seconds
    private int framesSinceCheck; // Number of frames since the last FPS update
    private int fps; // Current framerate

    public static void main(String[] args) {
        MAIN.run();
    }

    public static Main instance() {
        return MAIN;
    }

    private Main() {
        // Basic initialization happens here. GLFW init happens in initWindow()

        // Init logging
        logger = Logger.getLogger(getClass().getName());

        seed = initRandomSeed();
        logger.log(Level.INFO, "Random seed: " + seed);

        inputHandler = new InputHandler(this);
    }

    private void run() {
        try {
            initWindow();
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
     * Inits the GLFW window and OpenGL stuff
     */
    private void initWindow() {
        logger.log(Level.INFO, "Initializing window...");

        // There is a bug in certain parts of AWT that causes the program to hang when
        // initialized on MacOS. This prevents those parts from being initialized.
        System.setProperty("java.awt.headless", "true");

        // Setup error callback to print to System.err
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        // Configure the window
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // Disable using high DPI on retina displays. TODO remove this and support retina
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);

        // Set default size to half the monitor resolution
        final GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        windowWidth = vidmode.width() / 2;
        windowHeight = vidmode.height() / 2;

        // Create the window
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Terra", MemoryUtil.NULL,
                                       MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        GLFW.glfwSetWindowPos(window,
                              (vidmode.width() - windowWidth) / 2,
                              (vidmode.height() - windowHeight) / 2); // Center the window

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // Enable v-sync
        GLFW.glfwShowWindow(window); // Make the window visible
        GL.createCapabilities(); // LWJGL needs this
        GL11.glClearColor(Colors.CLEAR.getRed() / 255f, Colors.CLEAR.getGreen() / 255f,
                          Colors.CLEAR.getBlue() / 255f, Colors.CLEAR.getAlpha() / 255f);
        GL11.glOrtho(0, Renderer.RES_WIDTH, Renderer.RES_HEIGHT, 0, -1, 1);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Initialize input handlers
        GLFW.glfwSetKeyCallback(window, inputHandler::onKey);
        GLFW.glfwSetMouseButtonCallback(window, inputHandler::onMouseButton);
        GLFW.glfwSetScrollCallback(window, inputHandler::onScroll);
        GLFW.glfwSetCursorPosCallback(window, inputHandler::onCursorPos);
        GLFW.glfwSetFramebufferSizeCallback(window, inputHandler::onWindowResize);
    }

    /**
     * Initializes game stuff, i.e. anything that's not OpenGL/GLFW
     */
    private void initGame() {
        renderer = new Renderer();
        final WorldHandler worldHandler = new WorldHandler(getSeed());
        worldHandler.generate(); // Generate a world
        currentScreen = new WorldScreen(worldHandler); // Initialize the current screen
    }

    private void gameLoop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents(); // Poll for events (key, mouse, etc.)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear framebuffer
            currentScreen.draw(inputHandler.getMousePos());
            GLFW.glfwSwapBuffers(window); // Swap the color buffers

            // If the current screen says to change to another screen, do that
            final Screen nextScreen = currentScreen.getNextScreen();
            if (nextScreen != null) {
                currentScreen.resetNextScreen(); // We're changing screens so reset this
                currentScreen = nextScreen; // Go to the next screen
            }

            // If the current screen says the game should exit, then exit
            if (currentScreen.shouldExit()) {
                exitGame();
            }

            updateFPS(); // Update the FPS counter
        }
    }

    private void handleFatalException(Exception e) {
        logger.log(Level.SEVERE, "Fatal exception:", e);
    }

    private void tearDown() {
        // Renderer teardown
        if (renderer != null) {
            renderer.deleteTexturesAndFonts(); // Free up texture memory
        }

        GLFW.glfwDestroyWindow(window); // Destroy the window
        GLFW.glfwTerminate(); // Terminate GLFW
        GLFW.glfwSetErrorCallback(null).free(); // Need to wipe this out
    }

    /**
     * Exits the game gracefully.
     */
    private void exitGame() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }

    private void updateFPS() {
        framesSinceCheck++;

        // If it's been at least a second since the last loop...
        final double time = GLFW.glfwGetTime();
        if (time - lastFpsUpdate >= 1.0) {
            // Recalculate fps
            fps = framesSinceCheck;
            framesSinceCheck = 0;
            lastFpsUpdate = time;
        }
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public long getSeed() {
        return seed;
    }

    public Renderer renderer() {
        return renderer;
    }

    public int getFps() {
        return fps;
    }

    public void resizeWindow(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        GL11.glViewport(0, 0, windowWidth, windowHeight);
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public Point scaleMousePos(double xPos, double yPos) {
        return new Point(xPos * Renderer.RES_WIDTH / windowWidth,
                         yPos * Renderer.RES_HEIGHT / windowHeight);
    }
}