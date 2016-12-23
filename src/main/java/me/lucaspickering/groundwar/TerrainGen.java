package me.lucaspickering.groundwar;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.net.URL;
import java.util.Random;

import me.lucaspickering.groundwar.render.Renderer;
import me.lucaspickering.groundwar.render.event.KeyEvent;
import me.lucaspickering.groundwar.render.event.MouseButtonEvent;
import me.lucaspickering.groundwar.render.screen.MainScreen;
import me.lucaspickering.groundwar.render.screen.WorldScreen;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.world.World;

public class TerrainGen {

    private static final TerrainGen TERRAIN_GEN = new TerrainGen();

    // True if we are in debug mode (set by a VM argument)
    private final boolean debug;
    private final Random random;

    // These event handlers are initialized at the bottom
    private final GLFWKeyCallback keyHandler;
    private final GLFWMouseButtonCallback mouseButtonHandler;
    private final GLFWCursorPosCallback cursorPosHandler;
    private final GLFWWindowSizeCallback windowResizeHandler;

    private long window;
    private Renderer renderer;
    private MainScreen currentScreen;
    private int windowWidth;
    private int windowHeight;
    private Point mousePos = Point.ZERO;

    private World world;

    public static void main(String[] args) {
        TERRAIN_GEN.run();
    }

    public static TerrainGen instance() {
        return TERRAIN_GEN;
    }

    private TerrainGen() {
        // Basic initialization happens here. GLFW init happens in initWindow()

        // TODO argparsing
        // Check if we should be in debug mode
        debug = "true".equalsIgnoreCase(System.getProperty("debug"));

        // Set random instance
        // If a seed was provided, use that, otherwise use a default seed
        final String seedString = System.getProperty("seed");
        if (seedString != null) {
            long seed;
            // Try to parse the seed as a Long, and if that fails, just hash it
            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException e) {
                seed = seedString.hashCode();
            }
            random = new Random(seed);
        } else {
            random = new Random();
        }

        // Init event handlers
        keyHandler = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_RELEASE) {
                    currentScreen.onKey(new KeyEvent(window, key, scancode, mods));
                }
            }
        };
        mouseButtonHandler = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW.GLFW_RELEASE && currentScreen.contains(mousePos)) {
                    currentScreen.onClick(new MouseButtonEvent(window, button, mods, mousePos));
                }
            }
        };
        cursorPosHandler = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                // Scale the cursor coordinates to fit the coords that everything is drawn at.
                mousePos = new Point((int) (xPos * Constants.RES_WIDTH / windowWidth),
                                     (int) (yPos * Constants.RES_HEIGHT / windowHeight));
            }
        };
        windowResizeHandler = new GLFWWindowSizeCallback() {
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
            initWindow(); // Initialize
            gameLoop(); // Run the game
        } catch (Exception e) {
            System.err.println("Error in Terrain Gen:");
            e.printStackTrace();
        } finally {
            tearDown(); // Shutdown
        }
    }

    private void initWindow() {
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
        GL11.glOrtho(0, Constants.RES_WIDTH, Constants.RES_HEIGHT, 0, -1, 1);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Initialize input handlers
        GLFW.glfwSetKeyCallback(window, keyHandler);
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonHandler);
        GLFW.glfwSetCursorPosCallback(window, cursorPosHandler);
        GLFW.glfwSetWindowSizeCallback(window, windowResizeHandler);

        renderer = new Renderer();
        world = new World(); // Generate the world
        currentScreen = new WorldScreen(world); // Initialize the current screen
    }

    private void gameLoop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents(); // Poll for events (key, mouse, etc.)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear framebuffer
            currentScreen.draw(mousePos);
            GLFW.glfwSwapBuffers(window); // Swap the color buffers

            // Change to the next screen (usually nextScreen() returns the same screen)
            currentScreen = currentScreen.nextScreen();

            // If the current screen is null, exit the game
            if (currentScreen == null) {
                exitGame();
            }
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

    public boolean debug() {
        return debug;
    }

    public Random random() {
        return random;
    }

    public Renderer renderer() {
        return renderer;
    }

    /**
     * Exits the game gracefully.
     */
    private void exitGame() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }

    public static URL getResource(String path, String fileName) {
        return TerrainGen.class.getResource(String.format(path, fileName));
    }
}