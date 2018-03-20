package me.lucaspickering.terra.input;

import java.util.HashMap;
import java.util.Map;

public enum KeyActionGroup {

    ROOT(null, null),

    // World group
    WORLD("world", ROOT),
    WORLD_CAMERA("camera", WORLD),
    WORLD_TILECOLOR("tileColor", WORLD),
    WORLD_TILEOVERLAY("tileOverlay", WORLD);

    private final String name;
    private final KeyActionGroup parent;
    private final Map<String, KeyActionGroup> children = new HashMap<>();
    private final Map<String, KeyAction> actions = new HashMap<>();

    /**
     * Construct a new {@code KeyActionGroup}. A parent group can be given, in which case this
     * group will be considered a sub-group of the parent. If a parent is given, this group is
     * added to that parent's list of children.
     *
     * @param name   the name of the group
     * @param parent the parent group (or {@code null} for a top-level group)
     */
    KeyActionGroup(String name, KeyActionGroup parent) {
        this.name = name;
        this.parent = parent;

        // If there is a parent, save this group to it
        if (parent != null) {
            parent.children.put(name, this);
        }
    }

    public String getName() {
        return name;
    }

    public void addAction(KeyAction action) {
        actions.put(action.getName(), action);
    }

    private KeyAction getActionByNameHelper(String actionName) {
        final int dotIndex = actionName.indexOf('.'); // Find the first dot (if present)

        // If a dot is present, this is a group, so make a recursive call
        if (dotIndex > 0) {
            // Get everything before the dot
            final String groupName = actionName.substring(0, dotIndex);
            final KeyActionGroup subgroup = children.get(groupName);
            if (subgroup != null) {
                // Get everything after the dot
                final String rest = actionName.substring(dotIndex + 1);
                try {
                    return subgroup.getActionByNameHelper(rest); // Recursion!
                } catch (IllegalArgumentException e) {
                    // If the recursive call throws an error, the action name will be missing info
                    // Re-throw the error with more info
                    throw new IllegalArgumentException(String.format("Unknown action: %s",
                                                                     actionName),
                                                       e);
                }
            }
            // Fall through to exception
        } else {
            // No dot, this must be an action name
            final KeyAction action = actions.get(actionName);
            if (action != null) {
                return action;
            }
            // Fall through to exception
        }

        throw new IllegalArgumentException(String.format("Unknown action: %s", actionName));
    }

    public static KeyAction getActionByName(String actionName) {
        return ROOT.getActionByNameHelper(actionName);
    }
}
