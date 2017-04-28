package me.lucaspickering.terra.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.Main;
import me.lucaspickering.terra.render.screen.Screen;
import me.lucaspickering.terra.util.Constants;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.utils.Point;

public class InputHandler {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Main main;
    private final Map<Integer, String> inputMapping = new HashMap<>();
    private Point mousePos = Point.ZERO;

    public InputHandler(Main main) {
        this.main = main;
        loadConfig();
    }

    private void loadConfig() {
        final Properties prop = Funcs.loadProperties(Constants.CFG_KEYS);
        logger.log(Level.INFO, "Loaded key config");

        // Process the input from the properties class
        for (String propName : prop.stringPropertyNames()) {
            final String propValue = prop.getProperty(propName);
            final KeyMapping keyMap = KeyMapping.getByName(propValue);
            if (keyMap != null) {
                logger.log(Level.FINE, String.format("Mapping key [%s] to action [%s]",
                                                     keyMap, propName));
                inputMapping.put(keyMap.getCode(), propName);

            } else {
                logger.log(Level.WARNING, String.format("Unknown key [%s] for action [%s]",
                                                        propValue, propName));
            }
        }
    }

    public Point getMousePos() {
        return mousePos;
    }

    public void onKey(long window, int key, int scancode, int action, int mods) {
        final KeyEvent event = new KeyEvent(window, key, scancode,
                                            ButtonAction.getByGlfwCode(action), mods);
        main.getCurrentScreen().onKey(event);
    }

    public void onMouseButton(long window, int button, int action, int mods) {
        final Screen currentScreen = main.getCurrentScreen();
        if (currentScreen.contains(mousePos)) {
            final MouseButtonEvent event =
                new MouseButtonEvent(window, button, ButtonAction.getByGlfwCode(action),
                                     mods, mousePos);
            currentScreen.onClick(event);
        }
    }

    public void onScroll(long window, double xOffset, double yOffset) {
        final Screen currentScreen = main.getCurrentScreen();
        if (currentScreen.contains(mousePos)) {
            final ScrollEvent event = new ScrollEvent(window, xOffset, yOffset, mousePos);
            currentScreen.onScroll(event);
        }
    }

    public void onCursorPos(long window, double xPos, double yPos) {
        mousePos = main.scaleMousePos(xPos, yPos);
    }

    public void onWindowResize(long window, int width, int height) {
        main.resizeWindow(width, height);
    }
}
