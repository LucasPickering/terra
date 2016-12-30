package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.screen.gui.Button;
import me.lucaspickering.terraingen.render.screen.gui.GuiElement;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.terraingen.util.Point;

public class PauseScreen extends MainScreen {

    private final WorldScreen worldScreen;
    private final Button backToWorldButton;
    private final Button optionsButton;
    private final Button desktopButton;

    /**
     * Constructs a new PauseMenu. The given screen is the {@link WorldScreen} that will be returned
     * when the game is resumed.
     *
     * @param worldScreen the screen to eventually return to
     */
    public PauseScreen(WorldScreen worldScreen) {
        this.worldScreen = worldScreen;
        backToWorldButton = new Button("Resume", new Point(center.x(), center.y() - 200));
        optionsButton = new Button("Options", new Point(center.x(), center.y()));
        desktopButton = new Button("Exit to Desktop", new Point(center.x(), center.y() + 200));
        addGuiElement(backToWorldButton);
        addGuiElement(optionsButton);
        addGuiElement(desktopButton);
    }

    @Override
    public void draw(Point mousePos) {
        worldScreen.draw(Point.ZERO);

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
                    // Go back to the world
                    returnToWorld();
                    break;
            }
        }
    }

    @Override
    public void onElementClicked(MouseButtonEvent event, GuiElement element) {
        if (element == backToWorldButton) {
            returnToWorld(); // Go back to the world
        } else if (element == optionsButton) {
            // TODO options menu
        } else if (element == desktopButton) {
            setNextScreen(null); // Close the program
        }
    }

    private void returnToWorld() {
        // We need to set both of these so the world doesn't immediately re-open the pause menu
        worldScreen.setNextScreen(worldScreen);
        setNextScreen(worldScreen);
    }
}
