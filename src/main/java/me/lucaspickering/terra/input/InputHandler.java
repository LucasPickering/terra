package me.lucaspickering.terra.input;

import me.lucaspickering.terra.Main;
import me.lucaspickering.terra.render.screen.Screen;
import me.lucaspickering.utils.Point;

public class InputHandler {

    private final Main main;
    private Point mousePos = Point.ZERO;

    public InputHandler(Main main) {
        this.main = main;
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
