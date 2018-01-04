package me.lucaspickering.terra.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CameraController extends InputAdapter {

    private enum Action {
        CAMERA_FORWARD(Input.Keys.W) {
            @Override
            public void execute(Camera camera) {
                camera.translate()
            }
        },
        CAMERA_BACK(Input.Keys.S) {
            @Override
            public void execute(Camera camera) {

            }
        },
        CAMERA_LEFT(Input.Keys.A) {
            @Override
            public void execute(Camera camera) {

            }
        },
        CAMERA_RIGHT(Input.Keys.D) {
            @Override
            public void execute(Camera camera) {

            }
        },
        CAMERA_UP(Input.Keys.SPACE) {
            @Override
            public void execute(Camera camera) {

            }
        },
        CAMERA_DOWN(Input.Keys.SHIFT_LEFT) {
            @Override
            public void execute(Camera camera) {

            }
        };

        private static final Map<Integer, Action> keycodeMap = new HashMap<>();

        static {
            // Initialize a map of all actions keyed by keycode
            for (Action action : values()) {
                keycodeMap.put(action.keycode, action);
            }
        }

        private final int keycode;

        Action(int keycode) {
            this.keycode = keycode;


        }

        public static Action byKeycode(int keycode) {
            return keycodeMap.get(keycode);
        }

        public abstract void execute(Camera camera);

    }

    private static final float VELOCITY = 1f;

    private final Camera camera;
    private final Map<Action, Boolean> actionStates = new EnumMap<>(Action.class);

    public CameraController(Camera camera) {
        this.camera = camera;

        // Initialize all states to false
        for (Action action : Action.values()) {
            actionStates.put(action, false);
        }
    }

    public void update() {
        // Apply all actions that are currently active
        for (Map.Entry<Action, Boolean> entry : actionStates.entrySet()) {
            final Action action = entry.getKey();
            final boolean state = entry.getValue();
            if (state) {
                action.execute(camera);
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        final Action action = Action.byKeycode(keycode);
        if (action != null) {
            actionStates.put(action, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        final Action action = Action.byKeycode(keycode);
        if (action != null) {
            actionStates.put(action, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
