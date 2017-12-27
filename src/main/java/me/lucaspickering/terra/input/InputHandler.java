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
    private final Map<Integer, Command> keyMapping = new HashMap<>();
    private Point mousePos = Point.ZERO;

    public InputHandler(Main main) {
        this.main = main;
        loadConfig();
    }

    private void loadConfig() {
        final Properties prop = Funcs.loadProperties(Constants.CFG_KEYS);

        // Process the input from the config file
        Command.values(); // Force the Commands class to initialize now (I know this is ugly)
        for (String propName : prop.stringPropertyNames()) {
            final String propValue = prop.getProperty(propName);
            addInputMapping(propName, propValue);
        }
        logger.log(Level.INFO, "Loaded input config");
    }

    /**
     * Adds a mapping between the input with the given name and the command with the given name.
     *
     * @param commandString the name of the command (e.g. "game.menu")
     * @param input         the name of the input (e.g. "f" for the F key)
     */
    private void addInputMapping(String commandString, String input) {
        // Note that each if statement here checks an invalid condition - success requires that
        // every if condition FAILS
        final KeyMapping keyMap = KeyMapping.getByName(input);

        // Check that the input name is valid
        if (keyMap == null) {
            logger.log(Level.WARNING, String.format("Unknown input [%s] for command [%s]",
                                                    input, commandString));
            return;
        }

        final String[] commandParts = commandString.split("\\.", 2); // Split on '.' into two parts

        // Check if the split failed. If it did, the command is malformed.
        if (commandParts.length != 2) {
            logger.log(Level.WARNING, String.format("Malformed command [%s] for input [%s]",
                                                    commandString, input));
            return;
        }

        final String groupName = commandParts[0];
        final String commandName = commandParts[1];
        final CommandGroup group = CommandGroup.getByString(groupName);

        // Check if the command group name is valid
        if (group == null) {
            logger.log(Level.WARNING, String.format("Unknown command group [%s] for input [%s]",
                                                    groupName, commandString));
            return;
        }

        final Command command = group.getCommandByName(commandName);

        if (command == null) {
            logger.log(Level.WARNING, String.format(
                "Unknown command [%s] for group [%s] on input [%s]",
                commandName, groupName, input));
        }

        keyMapping.put(keyMap.getCode(), command);
        logger.log(Level.FINE, String.format("Mapping key [%s] to command [%s]",
                                             keyMap, input));
    }

    public Point getMousePos() {
        return mousePos;
    }

    public void onKey(long window, int key, int scancode, int action, int mods) {
        final Command command = keyMapping.get(key);

        // If this key is bound to a command, send an event to the game
        if (command != null) {
            final KeyEvent event = new KeyEvent(window, command, action, mods);
            main.getCurrentScreen().onKey(event);
        }
    }

    public void onMouseButton(long window, int button, int action, int mods) {
        final Screen currentScreen = main.getCurrentScreen();
        if (currentScreen.contains(mousePos)) {
            final MouseButtonEvent event = new MouseButtonEvent(window, button, action,
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
