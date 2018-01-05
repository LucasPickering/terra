package me.lucaspickering.terra.input;

import com.badlogic.gdx.InputAdapter;

import me.lucaspickering.terra.render.screen.ScreenHandler;

public class InputHandler extends InputAdapter {

    private final ScreenHandler screenHandler;

    public InputHandler(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    @Override
    public boolean keyDown(int keycode) {
        return screenHandler.getCurrentScreen() != null
               && screenHandler.getCurrentScreen().keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return screenHandler.getCurrentScreen() != null
               && screenHandler.getCurrentScreen().keyUp(keycode);
    }
}
