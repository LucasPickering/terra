package me.lucaspickering.terra.input;

public enum KeyAction {

    // world.camera
    WORLD_CAMERA_FORWARD("forward", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_BACK("back", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_LEFT("left", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_RIGHT("right", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_UP("up", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_DOWN("down", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_ROTATELEFT("rotateLeft", KeyActionGroup.WORLD_CAMERA),
    WORLD_CAMERA_ROTATERIGHT("rotateRight", KeyActionGroup.WORLD_CAMERA),

    // world.tileColor
    WORLD_TILECOLOR_COMPOSITE("composite", KeyActionGroup.WORLD_TILECOLOR),
    WORLD_TILECOLOR_BIOME("biome", KeyActionGroup.WORLD_TILECOLOR),
    WORLD_TILECOLOR_ELEVATION("elevation", KeyActionGroup.WORLD_TILECOLOR),
    WORLD_TILECOLOR_HUMIDITY("humidity", KeyActionGroup.WORLD_TILECOLOR),
    WORLD_TILECOLOR_RUNOFFLEVEL("runoffLevel", KeyActionGroup.WORLD_TILECOLOR),

    // world.tileOverlay
    WORLD_TILEOVERLAY_RUNOFFLEVEL("runoffLevel", KeyActionGroup.WORLD_TILEOVERLAY),
    WORLD_TILEOVERLAY_RUNOFFEXITS("runoffExits", KeyActionGroup.WORLD_TILEOVERLAY),
    WORLD_TILEOVERLAY_RUNOFFTERMINALS("runoffTerminals", KeyActionGroup.WORLD_TILEOVERLAY);


    private final String name;
    private final KeyActionGroup group;

    KeyAction(String name, KeyActionGroup group) {
        this.name = name;
        this.group = group;
        this.group.addAction(this); // Save this action to the group
    }

    public String getName() {
        return name;
    }

    public KeyActionGroup getGroup() {
        return group;
    }
}
