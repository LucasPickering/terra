package me.lucaspickering.terra.input;

import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;
import java.util.Map;

import me.lucaspickering.terra.render.screen.ScreenHandler;

public class InputHandler extends InputAdapter {

    private final ScreenHandler screenHandler;
    private final Map<Integer, KeyAction> keyToActionMappings = new HashMap<>();

    public InputHandler(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;

        // Convert int:string mappings into int:KeyAction mappings
        for (Map.Entry<Integer, String> entry : KeyBindings.KEY_BINDINGS.entrySet()) {
            final int keycode = entry.getKey();
            final String actionName = entry.getValue();
            keyToActionMappings.put(keycode, KeyActionGroup.getActionByName(actionName));
        }
    }

    private KeyAction keycodeToAction(int keycode) {
        return keyToActionMappings.get(keycode);
    }

    @Override
    public boolean keyDown(int keycode) {
        return screenHandler.getCurrentScreen() != null
               && screenHandler.getCurrentScreen().keyDown(keycodeToAction(keycode));
    }

    @Override
    public boolean keyUp(int keycode) {
        return screenHandler.getCurrentScreen() != null
               && screenHandler.getCurrentScreen().keyUp(keycodeToAction(keycode));
    }
}
