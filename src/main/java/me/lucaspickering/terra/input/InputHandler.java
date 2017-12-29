package me.lucaspickering.terra.input;

import org.apache.commons.configuration2.SubnodeConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.Main;
import me.lucaspickering.terra.render.screen.Screen;
import me.lucaspickering.terra.util.Constants;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.utils.Point2;

public class InputHandler {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Main main;
    private final Map<Integer, Command> keyMapping = new HashMap<>();
    private Point2 mousePos = Point2.ZERO;

    public InputHandler(Main main) {
        this.main = main;
        loadConfig();
    }

    private void loadConfig() {
        Command.values(); // We have to do this to get the enum to initialize

        final Map<String, SubnodeConfiguration> cfg = Funcs.loadConfig(Constants.CFG_KEYS);
        for (Map.Entry<String, SubnodeConfiguration> entry : cfg.entrySet()) {
            final String sectionName = entry.getKey();
            final SubnodeConfiguration values = entry.getValue();

            // Get the command group by name. If it doesn't exist, log an error and skip
            final CommandGroup group = CommandGroup.getByString(sectionName);
            if (group == null) {
                logger.log(Level.WARNING, String.format("Unknown command group [%s]", sectionName));
                continue; // Skip this group
            }

            // Register each command and its mapping for this section
            for (String key : (Iterable<String>) values::getKeys) {
                addInputMapping(group, key, values.getString(key));
            }
        }

        logger.log(Level.INFO, "Loaded input config");
    }

    /**
     * Adds a mapping between the input with the given name and the command with the given name.
     *
     * @param group       the {@link CommandGroup} for the given command (e.g. GAME)
     * @param commandName the name of the command (e.g. "menu")
     * @param input       the name of the input (e.g. "f" for the F key)
     */
    private void addInputMapping(CommandGroup group, String commandName, String input) {
        // Note that each if statement here checks an invalid condition - success requires that
        // every if condition FAILS
        final KeyMapping keyMap = KeyMapping.getByName(input);

        // Check that the input name is valid
        if (keyMap == null) {
            logger.log(Level.WARNING, String.format("Unknown input [%s] for command [%s]",
                                                    input, commandName));
            return;
        }

        final Command command = group.getCommandByName(commandName);

        if (command == null) {
            logger.log(Level.WARNING, String.format(
                "Unknown command [%s] for group [%s] on input [%s]", commandName, group, input));
        }

        keyMapping.put(keyMap.getCode(), command);
        logger.log(Level.FINE, String.format("Mapping key [%s] to command [%s]", keyMap, input));
    }

    public Point2 getMousePos() {
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
