package me.lucaspickering.terra.render.screen;

import org.lwjgl.opengl.GL11;

import me.lucaspickering.terra.input.ButtonAction;
import me.lucaspickering.terra.input.KeyEvent;
import me.lucaspickering.terra.input.MouseButtonEvent;
import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.render.screen.gui.Button;
import me.lucaspickering.terra.render.screen.gui.GuiElement;
import me.lucaspickering.terra.util.Colors;
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
        if (event.action == ButtonAction.RELEASE) {
            switch (event.command) {
                case GAME_MENU:
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
