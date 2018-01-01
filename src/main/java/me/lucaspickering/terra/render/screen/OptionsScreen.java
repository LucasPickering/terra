package me.lucaspickering.terra.render.screen;

import me.lucaspickering.terra.input.ButtonAction;
import me.lucaspickering.terra.input.KeyEvent;
import me.lucaspickering.terra.render.screen.gui.Button;

public class OptionsScreen extends MenuScreen {

    /**
     * Constructs a new {@code OptionsScreen}. The given background screen is rendered in the
     * background of this menu. The given previous screen is the screen that will be returned to
     * when this one is closed.
     *
     * @param bgScreen   the screen to render in the background
     * @param prevScreen the screen to eventually return to
     */
    public OptionsScreen(Screen bgScreen, Screen prevScreen) {
        super(bgScreen, prevScreen);

        // Add Back button
        addGuiElement(new Button.Builder()
                          .text("Back")
                          .pos(CENTER)
                          .clickHandler(event -> returnToPrev()) // Go back to previous menu
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
