package me.lucaspickering.terra.render;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.InputHandler;
import me.lucaspickering.terra.render.screen.Screen;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.utils.Point2;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final Logger logger;
    private final InputHandler inputHandler;
    private final String title;

    private Screen currentScreen;

    private long windowHandle;
    private int width;
    private int height;
    private boolean vsync;

    public Window(String title, int width, int height, boolean vsync) {
        logger = Logger.getLogger(getClass().getName());
        inputHandler = new InputHandler(this);
        this.title = title;
        this.width = width;
        this.height = height;
        this.vsync = vsync;
    }

    public void init() {
        logger.log(Level.INFO, "Initializing window...");

        // There is a bug in certain parts of AWT that causes the program to hang when
        // initialized on MacOS. This prevents those parts from being initialized.
        System.setProperty("java.awt.headless", "true");

        // Setup error callback to print to System.err
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        // Initialize  Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        // Configure the window
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Disable using high DPI on retina displays. TODO remove this and support retina
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Center the window
        final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowHandle,
                         (vidmode.width() - width) / 2,
                         (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(windowHandle);
        glfwShowWindow(windowHandle); // Make the window visible
        GL.createCapabilities(); // LWJGL needs this
        GL11.glClearColor(Colors.CLEAR.getRed() / 255f, Colors.CLEAR.getGreen() / 255f,
                          Colors.CLEAR.getBlue() / 255f, Colors.CLEAR.getAlpha() / 255f);
        GL11.glOrtho(0, Renderer.RES_WIDTH, Renderer.RES_HEIGHT, 0, -1, 1);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (vsync) {
            glfwSwapInterval(1); // Enable v-sync
        }

        // Initialize input handlers
        glfwSetKeyCallback(windowHandle, inputHandler::onKey);
        glfwSetMouseButtonCallback(windowHandle, inputHandler::onMouseButton);
        glfwSetScrollCallback(windowHandle, inputHandler::onScroll);
        glfwSetCursorPosCallback(windowHandle, inputHandler::onCursorPos);
        glfwSetFramebufferSizeCallback(windowHandle, inputHandler::onWindowResize);
    }

    public void update() {
        glfwPollEvents(); // Poll for events (key, mouse, etc.)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear framebuffer
        currentScreen.draw(inputHandler.getMousePos());
        glfwSwapBuffers(windowHandle);

        // If the current screen says to change to another screen, do that
        final Screen nextScreen = currentScreen.getNextScreen();
        if (nextScreen != null) {
            currentScreen.resetNextScreen(); // We're changing screens so reset this
            currentScreen = nextScreen; // Go to the next screen
        }

        // If the current screen says the game should exit, then exit
        if (currentScreen.shouldExit()) {
            exit();
        }
    }

    public void cleanup() {
        glfwDestroyWindow(windowHandle); // Destroy the window
        glfwTerminate(); // Terminate GLFW
        glfwSetErrorCallback(null).free(); // Need to wipe this out
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Exits the program gracefully.
     */
    public void exit() {
        glfwSetWindowShouldClose(windowHandle, true);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        GL11.glViewport(0, 0, width, height);
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(Screen screen) {
        currentScreen = screen;
    }

    public Point2 scaleMousePos(double xPos, double yPos) {
        return new Point2(xPos * Renderer.RES_WIDTH / width,
                          yPos * Renderer.RES_HEIGHT / height);
    }
}
