package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.screen.gui.Button;
import me.lucaspickering.terraingen.render.screen.gui.GuiElement;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.utils.Point;

public class OptionsScreen extends Screen {

    private final WorldScreen worldScreen;
    private final Screen prevScreen;
    private final Button backButton;

    /**
     * Constructs a new {@code PauseScreen}. The given {@link WorldScreen} is rendered in the
     * background of this menu. The given {@link Screen} is the screen that will be returned to
     * when this one is closed.
     *
     * @param worldScreen the world to render in the background
     * @param prevScreen  the screen to eventually return to
     */
    public OptionsScreen(WorldScreen worldScreen, Screen prevScreen) {
        this.worldScreen = worldScreen;
        this.prevScreen = prevScreen;

        // Initialize everything
        backButton = new Button("Back", new Point(center.x(), center.y()));

        // Add all the elements
        addGuiElement(backButton);
    }

    @Override
    public void draw(Point mousePos) {
        worldScreen.draw(null);

        GL11.glEnable(GL11.GL_BLEND);
        renderer().drawRect(0, 0, Renderer.RES_WIDTH, Renderer.RES_HEIGHT, Colors.MENU_SHADER);
        GL11.glDisable(GL11.GL_BLEND);

        super.draw(mousePos);
    }

    @Override
    public void onKey(KeyEvent event) {
        if (event.action == GLFW.GLFW_RELEASE) {
            switch (event.key) {
                case GLFW.GLFW_KEY_ESCAPE:
                    returnToPrev();
                    break;
            }
        }
        super.onKey(event);
    }

    @Override
    public void onElementClicked(MouseButtonEvent event, GuiElement element) {
        if (element == backButton) {
            returnToPrev();
        }
    }

    private void returnToPrev() {
        setNextScreen(prevScreen); // Go back to the world
    }
}
