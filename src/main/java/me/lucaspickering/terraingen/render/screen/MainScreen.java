package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.screen.gui.GuiElement;
import me.lucaspickering.terraingen.util.Point;

/**
 * A {@code MainScreen} is a type of {@link ScreenElement} that is meant to be a top-level element.
 * A {@code MainScreen} has no parent {@link ScreenElement} and there can only ever be one active
 * {@code MainScreen} at a time. Examples of a {@code MainScreen} include the main menu screen and
 * the in-game screen.
 */
public abstract class MainScreen implements ScreenElement {

    protected final Point center = new Point(Renderer.RES_WIDTH / 2,
                                             Renderer.RES_HEIGHT / 2);
    private List<GuiElement> guiElements = new LinkedList<>();
    private MainScreen nextScreen = this;

    @Override
    public void draw(Point mousePos) {
        // Draw all visible GUI elements
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        guiElements.stream().filter(GuiElement::isVisible).forEach(element -> drawElement(mousePos,
                                                                                          element));
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawElement(Point mousePos, GuiElement element) {
        GL11.glPushMatrix();
        final Point pos = element.getPos();
        GL11.glTranslatef(pos.x(), pos.y(), 0f);
        element.draw(mousePos);
        GL11.glPopMatrix();
    }

    protected final void setNextScreen(MainScreen nextScreen) {
        this.nextScreen = nextScreen;
    }

    /**
     * Called each frame by the main game loop, after {@link #draw}. To keep this screen as the
     * current screen, return {@code null}. To change to another screen, return that screen. To keep
     * this screen, return {@code this}. To exit the game, return {@code null}
     *
     * @return the screen to change to, {@code this} to keep this screen, or {@code null} to exit
     * the game
     */
    public final MainScreen nextScreen() {
        return nextScreen;
    }

    @Override
    public boolean contains(Point p) {
        return true;
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
        // By default, nothing is done on key press
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
     * Called when an element in{@link #guiElements} is clicked.
     *
     * @param event   the event that occurred
     * @param element the element that was clicked
     */
    protected void onElementClicked(MouseButtonEvent event, GuiElement element) {
        // By default, do nothing
    }
}
