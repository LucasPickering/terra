package me.lucaspickering.terraingen.render.screen;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Font;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.event.ScrollEvent;
import me.lucaspickering.terraingen.render.screen.gui.GuiElement;
import me.lucaspickering.utils.Point;

/**
 * A {@code Screen} is a type of {@link ScreenElement} that is meant to be a top-level element.
 * A {@code Screen} has no parent {@link ScreenElement} and there can only ever be one active
 * {@code Screen} at a time. Examples of a {@code Screen} include the main menu screen and
 * the in-game screen.
 */
public abstract class Screen implements ScreenElement {

    private static final String DEBUG_FORMAT = "FPS: %d";

    protected final Point center = new Point(Renderer.RES_WIDTH / 2,
                                             Renderer.RES_HEIGHT / 2);
    private final TerrainGen terrainGen = TerrainGen.instance();
    private List<GuiElement> guiElements = new LinkedList<>();
    private Screen nextScreen;
    private boolean shouldExit; // Set to true to close the game

    @Override
    public void draw(Point mousePos) {
        // Draw all visible GUI elements
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        guiElements.stream()
            .filter(GuiElement::isVisible) // Only draw elements that are visible
            .forEach(element -> drawElement(mousePos, element)); // Draw each element

        // If debug mode is enabled, draw debug info
        if (getTerrainGen().getDebug()) {
            drawDebugInfo();
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawElement(Point mousePos, GuiElement element) {
        GL11.glPushMatrix();
        final Point pos = element.getPos();
        GL11.glTranslated(pos.x(), pos.y(), 0f);
        element.draw(mousePos);
        GL11.glPopMatrix();
    }

    private void drawDebugInfo() {
        final String debugString = String.format(DEBUG_FORMAT, getTerrainGen().getFps());
        renderer().drawString(Font.DEBUG, debugString, 10, 10); // Draw FPS
    }

    public final TerrainGen getTerrainGen() {
        return terrainGen;
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

    @Override
    public boolean contains(Point p) {
        return 0 <= p.x() && p.x() <= Renderer.RES_WIDTH
               && 0 <= p.y() && p.y() <= Renderer.RES_HEIGHT;
    }


    protected final void addGuiElement(GuiElement element) {
        guiElements.add(element);
    }

    /**
     * Called when a key is pressed.
     *
     * @param event the event that occurred
     */
    public void onKey(KeyEvent event) {
        if (event.action == GLFW.GLFW_RELEASE) {
            switch (event.key) {
                case GLFW.GLFW_KEY_F9:
                    // Toggle debug mode
                    final TerrainGen terrainGen = getTerrainGen();
                    terrainGen.setDebug(!terrainGen.getDebug());
                    break;
            }
        }
    }

    /**
     * Called when this element is clicked.
     *
     * @param event the event that occurred
     */
    public void onClick(MouseButtonEvent event) {
        // Call onElementClicked for all GUI elements that contain the cursor
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_1 && event.action == GLFW.GLFW_RELEASE) {
            guiElements.stream()
                .filter(element -> element.isEnabled() && element.contains(event.mousePos))
                .forEach(element -> onElementClicked(event, element));
        }
    }

    /**
     * Called when the user scrolls while the mouse is over this element.
     *
     * @param event the event that occurred
     */
    public void onScroll(ScrollEvent event) {
        // By default, do nothing
    }

    /**
     * Called when an element in{@link #guiElements} is clicked.
     *
     * @param event   the event that occurred
     * @param element the element that was clicked
     */
    protected void onElementClicked(MouseButtonEvent event, GuiElement element) {
        // By default, do nothing
    }
}
