package me.lucaspickering.terra.render.screen;

import org.lwjgl.opengl.GL11;

import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.utils.Point2;

abstract class MenuScreen extends Screen {

    private final Screen bgScreen;
    private final Screen prevScreen;

    /**
     * Constructs a new {@code MenuScreen}. The given background screen is rendered in the
     * background of this menu. The given previous screen is the screen that will be returned to
     * when this one is closed.
     *
     * @param bgScreen   the screen to render in the background (null for none)
     * @param prevScreen the screen to eventually return to (null for none)
     */
    public MenuScreen(Screen bgScreen, Screen prevScreen) {
        this.bgScreen = bgScreen;
        this.prevScreen = prevScreen;
    }

    @Override
    public void draw(Point2 mousePos) {
        if (bgScreen != null) {
            bgScreen.draw(null);
        }

        GL11.glEnable(GL11.GL_BLEND);
        renderer().drawRect(0, 0, Renderer.RES_WIDTH, Renderer.RES_HEIGHT, Colors.MENU_SHADER);
        GL11.glDisable(GL11.GL_BLEND);

        super.draw(mousePos);
    }

    protected Screen getBgScreen() {
        return bgScreen;
    }

    protected Screen getPrevScreen() {
        return prevScreen;
    }

    /**
     * Return to the previous screen.
     *
     * @throws NullPointerException if there is no previous screen
     */
    protected void returnToPrev() {
        // If prevScreen is null but we try to return, that's a no bueno
        if (prevScreen == null) {
            throw new NullPointerException("Cannot return to previous screen because it is null");
        }
        setNextScreen(prevScreen); // Go back to the previous screen
    }
}
