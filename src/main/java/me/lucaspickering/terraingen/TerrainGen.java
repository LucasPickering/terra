package me.lucaspickering.terraingen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.event.ScrollEvent;
import me.lucaspickering.terraingen.render.screen.Screen;
import me.lucaspickering.terraingen.render.screen.WorldScreen;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.world.World;

public class TerrainGen {

    private static final TerrainGen TERRAIN_GEN = new TerrainGen();

    private final Logger logger;
    private final boolean debug; // True if we are in debug mode (set by a VM argument)
    private final long seed;

    // These event handlers are initialized at the bottom
    private final GLFWKeyCallback keyHandler;
    private final GLFWMouseButtonCallback mouseButtonHandler;
    private final GLFWScrollCallback scrollHandler;
    private final GLFWCursorPosCallback cursorPosHandler;
    private final GLFWFramebufferSizeCallback windowResizeHandler;

    private long window;
    private Renderer renderer;
    private Screen currentScreen;
    private int windowWidth;
    private int windowHeight;
    private Point mousePos = Point.ZERO;
    private double lastFpsUpdate; // Time of the last FPS update, in seconds
    private int framesSinceCheck; // Number of frames since the last FPS update
    private int fps; // Current framerate

    public static void main(String[] args) {
        TERRAIN_GEN.run();
    }

    public static TerrainGen instance() {
        return TERRAIN_GEN;
    }

    private TerrainGen() {
        // Basic initialization happens here. GLFW init happens in initWindow()

        // Init logging
        logger = Logger.getLogger(getClass().getName());

        // Check if we should be in debug mode ("me.lucaspickering.terraingen.debug")
        debug = "true".equalsIgnoreCase(System.getProperty(getClass().getPackage().getName() +
                                                           ".debug"));
        if (debug) {
            logger.log(Level.FINE, "Debug mode enabled");
        }

        seed = initRandomSeed();
        logger.log(Level.CONFIG, "Random seed: " + seed);

        // Init event handlers
        keyHandler = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                currentScreen.onKey(new KeyEvent(window, key, scancode, action, mods));
            }
        };
        mouseButtonHandler = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (currentScreen.contains(mousePos)) {
                    currentScreen.onClick(new MouseButtonEvent(window, button, action, mods,
                                                               mousePos));
                }
            }
        };
        scrollHandler = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                if (currentScreen.contains(mousePos)) {
                    currentScreen.onScroll(new ScrollEvent(window, xOffset, yOffset, mousePos));
                }
            }
        };
        cursorPosHandler = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                // Scale the cursor coordinates to fit the coords that everything is drawn at.
                mousePos = new Point((int) (xPos * Renderer.RES_WIDTH / windowWidth),
                                     (int) (yPos * Renderer.RES_HEIGHT / windowHeight));
            }
        };
        windowResizeHandler = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                windowWidth = width;
                windowHeight = height;
                GL11.glViewport(0, 0, windowWidth, windowHeight);
            }
        };
    }

    private void run() {
        try {
            initWindow(); // Initialize the window
            gameLoop(); // Run the game
        } catch (Exception e) {
            System.err.println("Error in Terrain Gen:");
            e.printStackTrace();
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

    private void initWindow() {
        logger.log(Level.FINE, "Initializing window...");

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

        // Set default size to half the monitor resolution
        final GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        windowWidth = vidmode.width() / 2;
        windowHeight = vidmode.height() / 2;

        // Create the window
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Terrain Gen", MemoryUtil.NULL,
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
        GLFW.glfwSetKeyCallback(window, keyHandler);
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonHandler);
        GLFW.glfwSetScrollCallback(window, scrollHandler);
        GLFW.glfwSetCursorPosCallback(window, cursorPosHandler);
        GLFW.glfwSetFramebufferSizeCallback(window, windowResizeHandler);

        renderer = new Renderer();
        final World world = new World(getSeed());
        currentScreen = new WorldScreen(world); // Initialize the current screen
        lastFpsUpdate = GLFW.glfwGetTime(); // Set this for FPS calculation
    }

    private void gameLoop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents(); // Poll for events (key, mouse, etc.)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear framebuffer
            currentScreen.draw(mousePos);
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

    private void tearDown() {
        // Renderer teardown
        if (renderer != null) {
            renderer.deleteTexturesAndFonts(); // Free up texture memory
        }

        // Release callbacks
        keyHandler.free();
        mouseButtonHandler.free();
        cursorPosHandler.free();
        windowResizeHandler.free();

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
        // If it's been more than a second since the last loop...
        if (GLFW.glfwGetTime() - lastFpsUpdate >= 1.0) {
            // Recalculate fps
            fps = framesSinceCheck;
            framesSinceCheck = 0;
            lastFpsUpdate++;
        }
        framesSinceCheck++;
    }

    public boolean debug() {
        return debug;
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
}