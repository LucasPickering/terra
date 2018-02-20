package me.lucaspickering.terra.input;

import com.badlogic.gdx.Input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to store all key-string:action-string mappings. May be replaced by an editable config
 * file one day, but this will suffice for now.
 */
public class KeyBindings {

    public static final Map<Integer, String> KEY_BINDINGS;

    static {
        // Build a map with all the bindings
        final Map<Integer, String> keyBindings = new HashMap<>() {{
            put(Input.Keys.W, "world.camera.forward");
            put(Input.Keys.S, "world.camera.back");
            put(Input.Keys.A, "world.camera.left");
            put(Input.Keys.D, "world.camera.right");
            put(Input.Keys.SPACE, "world.camera.up");
            put(Input.Keys.SHIFT_LEFT, "world.camera.down");
            put(Input.Keys.LEFT, "world.camera.rotateLeft");
            put(Input.Keys.RIGHT, "world.camera.rotateRight");

            put(Input.Keys.NUM_1, "world.tileColor.composite");
            put(Input.Keys.NUM_2, "world.tileColor.biome");
            put(Input.Keys.NUM_3, "world.tileColor.elevation");
            put(Input.Keys.NUM_4, "world.tileColor.humidity");
            put(Input.Keys.NUM_5, "world.tileColor.runoffLevel");

            put(Input.Keys.NUM_7, "world.tileOverlay.runoffLevel");
            put(Input.Keys.NUM_8, "world.tileOverlay.runoffExits");
            put(Input.Keys.NUM_9, "world.tileOverlay.runoffTerminals");
        }};

        // Make an immutable version of the map for public use
        KEY_BINDINGS = Collections.unmodifiableMap(keyBindings);
    }
}
