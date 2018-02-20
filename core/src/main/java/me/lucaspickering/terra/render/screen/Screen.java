package me.lucaspickering.terra.render.screen;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import me.lucaspickering.terra.input.KeyAction;
import me.lucaspickering.utils.Point2;

/**
 * A {@code Screen} is a type of {@link ScreenElement} that is meant to be a top-level element. A
 * {@code Screen} has no parent {@link ScreenElement} and there can only ever be one active {@code
 * Screen} at a time. Examples of a {@code Screen} include the main menu screen and the in-game
 * screen.
 */
public abstract class Screen implements ScreenElement {

    private final Map<KeyAction, Runnable> keyActions = new EnumMap<>(KeyAction.class);
    private Screen nextScreen;
    private boolean shouldExit; // Set to true to close the game

    @Override
    public boolean contains(Point2 p) {
        return true;
    }

    /**
     * Register the given function to be called when the given key action occurs.
     *
     * @param action  the action to be called on
     * @param handler the function to be called when the action occurs
     */
    protected void registerKeyAction(KeyAction action, Runnable handler) {
        keyActions.put(action, handler);
    }

    /**
     * Called each frame by the main game loop, after {@link #draw}. To keep this screen as the
     * current screen, return {@code null}. To change to another screen, return that screen.
     *
     * @return the screen to change to, or {@code null} to keep this screen
     */
    public final Screen getNextScreen() {
        return nextScreen;
    }

    /**
     * Resets the next screen back to null. Should be called on the old screen after changing
     * screens, if you intend to use that same object again.
     */
    public final void resetNextScreen() {
        nextScreen = null; // Reset it to null
    }

    /**
     * Sets the next screen, which will be swapped to.
     *
     * @param nextScreen the next screen to display
     */
    protected final void setNextScreen(@NotNull Screen nextScreen) {
        Objects.requireNonNull(nextScreen);
        this.nextScreen = nextScreen;
    }

    /**
     * Should the game exit?
     *
     * @return {@code true} if the game should exit, {@code false} otherwise
     */
    public final boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Closes the game.
     */
    protected final void exit() {
        shouldExit = true;
    }

    /**
     * Free any resources this screen may have allocated.
     */
    public void dispose() {
        // Do nothing by default
    }

    public boolean keyDown(KeyAction action) {
        // If the action is registered, run it and return true
        if (keyActions.containsKey(action)) {
            keyActions.get(action).run();
            return true;
        }

        return false;
    }

    public boolean keyUp(KeyAction action) {
        return false;
    }
}
