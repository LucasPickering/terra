package me.lucaspickering.terra.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CameraController extends GestureDetector {

    private enum CameraMovement {
        FORWARD(Input.Keys.W) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, 0f, -MOVE_VELOCITY), camera));
            }
        },
        BACK(Input.Keys.S) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, 0f, MOVE_VELOCITY), camera));
            }
        },
        LEFT(Input.Keys.A) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(-MOVE_VELOCITY, 0f, 0f), camera));
            }
        },
        RIGHT(Input.Keys.D) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(MOVE_VELOCITY, 0f, 0f), camera));
            }
        },
        UP(Input.Keys.SPACE) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, MOVE_VELOCITY, 0f), camera));
            }
        },
        DOWN(Input.Keys.SHIFT_LEFT) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, -MOVE_VELOCITY, 0f), camera));
            }
        },
        ROTATE_LEFT(Input.Keys.LEFT) {
            @Override
            public void transform(Camera camera) {
                camera.rotate(ROTATE_VELOCITY, 0f, 1f, 0f);
            }
        },
        ROTATE_RIGHT(Input.Keys.RIGHT) {
            @Override
            public void transform(Camera camera) {
                camera.rotate(-ROTATE_VELOCITY, 0f, 1f, 0f);
            }
        };

        private static final Map<Integer, CameraMovement> keycodeMap = new HashMap<>();

        static {
            // Initialize a map of all actions keyed by keycode
            for (CameraMovement cameraMovement : values()) {
                keycodeMap.put(cameraMovement.keycode, cameraMovement);
            }
        }

        private final int keycode;

        CameraMovement(int keycode) {
            this.keycode = keycode;
        }

        public static CameraMovement byKeycode(int keycode) {
            return keycodeMap.get(keycode);
        }

        private static Vector3 alignToCamera(Vector3 vec, Camera camera) {
            // Get the vector's components in the xz-plane
            final Vector2 xz = new Vector2(camera.direction.x, camera.direction.z);
            // Figure out how much the vector needs to be rotated around the y axis
            final float angle = -xz.angle() - 90;
            return vec.rotate(angle, 0f, 1f, 0f); // Rotato potato
        }

        public abstract void transform(Camera camera);
    }

    private static final float MOVE_VELOCITY = 10f;
    private static final float ROTATE_VELOCITY = 1f;

    private final Camera camera;
    private final Map<CameraMovement, Boolean> actionStates = new EnumMap<>(CameraMovement.class);

    public CameraController(Camera camera) {
        super(new GestureAdapter());
        this.camera = camera;

        // Initialize all states to false
        for (CameraMovement cameraMovement : CameraMovement.values()) {
            actionStates.put(cameraMovement, false);
        }
    }

    public void update() {
        // Apply transformation for all active actions
        actionStates.entrySet().stream()
            .filter(Map.Entry::getValue) // Filter out inactive actions
            .forEach(e -> e.getKey().transform(camera)); // Apply each transformation

        camera.update();
    }

    @Override
    public boolean keyDown(int keycode) {
        final CameraMovement cameraMovement = CameraMovement.byKeycode(keycode);
        if (cameraMovement != null) {
            actionStates.put(cameraMovement, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        final CameraMovement cameraMovement = CameraMovement.byKeycode(keycode);
        if (cameraMovement != null) {
            actionStates.put(cameraMovement, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
