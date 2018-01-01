package me.lucaspickering.terra.render.screen;

import me.lucaspickering.terra.input.ButtonAction;
import me.lucaspickering.terra.input.KeyEvent;
import me.lucaspickering.terra.render.screen.gui.Button;

public class PauseScreen extends MenuScreen {

    /**
     * Constructs a new {@code PauseScreen}. The given screen is the one that was open before this,
     * which will be renderer in the background and returned to when this screen is exited.
     *
     * @param prevScreen the screen to eventually return to
     */
    public PauseScreen(Screen prevScreen) {
        super(prevScreen, prevScreen);

        // Resume button
        addGuiElement(new Button.Builder()
                          .text("Resume")
                          .pos(CENTER.plus(0, -200))
                          .clickHandler(event -> returnToPrev()) // Return to last menu
                          .build());

        // Options Menu button
        addGuiElement(new Button.Builder()
                          .text("Options")
                          .pos(CENTER)
                          .clickHandler(event -> setNextScreen(
                              new OptionsScreen(getBgScreen(), this))) // Go to options menu
                          .build());

        // Exit to Desktop button
        addGuiElement(new Button.Builder()
                          .text("Exit to Desktop")
                          .pos(CENTER.plus(0, 200))
                          .clickHandler(event -> exit()) // Exit the program
                          .build());
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
}
