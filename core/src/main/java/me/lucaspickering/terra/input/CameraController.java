package me.lucaspickering.terra.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.EnumMap;
import java.util.Map;

public class CameraController extends GestureDetector {

    private enum CameraMovement {
        FORWARD(KeyAction.WORLD_CAMERA_FORWARD) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, 0f, -MOVE_VELOCITY), camera));
            }
        },
        BACK(KeyAction.WORLD_CAMERA_BACK) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, 0f, MOVE_VELOCITY), camera));
            }
        },
        LEFT(KeyAction.WORLD_CAMERA_LEFT) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(-MOVE_VELOCITY, 0f, 0f), camera));
            }
        },
        RIGHT(KeyAction.WORLD_CAMERA_RIGHT) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(MOVE_VELOCITY, 0f, 0f), camera));
            }
        },
        UP(KeyAction.WORLD_CAMERA_UP) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, MOVE_VELOCITY, 0f), camera));
            }
        },
        DOWN(KeyAction.WORLD_CAMERA_DOWN) {
            @Override
            public void transform(Camera camera) {
                camera.translate(alignToCamera(new Vector3(0f, -MOVE_VELOCITY, 0f), camera));
            }
        },
        ROTATE_LEFT(KeyAction.WORLD_CAMERA_ROTATELEFT) {
            @Override
            public void transform(Camera camera) {
                camera.rotate(ROTATE_VELOCITY, 0f, 1f, 0f);
            }
        },
        ROTATE_RIGHT(KeyAction.WORLD_CAMERA_ROTATERIGHT) {
            @Override
            public void transform(Camera camera) {
                camera.rotate(-ROTATE_VELOCITY, 0f, 1f, 0f);
            }
        };

        private static final Map<KeyAction, CameraMovement> actionMap =
            new EnumMap<>(KeyAction.class);

        static {
            // Initialize a map of all actions keyed by keycode
            for (CameraMovement cameraMovement : values()) {
                actionMap.put(cameraMovement.action, cameraMovement);
            }
        }

        private final KeyAction action;

        CameraMovement(KeyAction action) {
            this.action = action;
        }

        public static CameraMovement byAction(KeyAction action) {
            return actionMap.get(action);
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

    private static final float MOVE_VELOCITY = 100f;
    private static final float ROTATE_VELOCITY = 2f;

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

    public boolean keyDown(KeyAction action) {
        final CameraMovement cameraMovement = CameraMovement.byAction(action);
        if (cameraMovement != null) {
            actionStates.put(cameraMovement, true);
            return true;
        }
        return false;
    }

    public boolean keyUp(KeyAction action) {
        final CameraMovement cameraMovement = CameraMovement.byAction(action);
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
