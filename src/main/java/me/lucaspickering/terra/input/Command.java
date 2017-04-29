package me.lucaspickering.terra.input;

public enum Command {

    GAME_MENU(CommandGroup.GAME, "menu"),

    WORLD_DEBUG(CommandGroup.WORLD, "debug"),
    WORLD_REGEN(CommandGroup.WORLD, "regenerate"),
    WORLD_NEXT_STEP(CommandGroup.WORLD, "nextStep"),
    WORLD_TILE_COLOR_ELEVATION(CommandGroup.WORLD, "tileColorElevation"),
    WORLD_TILE_COLOR_HUMIDITY(CommandGroup.WORLD, "tileColorHumidity"),
    WORLD_TILE_COLOR_WATER_LEVEL(CommandGroup.WORLD, "tileColorWaterLevel"),
    WORLD_TILE_COLOR_WATER_TRAVERSED(CommandGroup.WORLD, "tileColorWaterTraversed"),
    WORLD_TILE_COLOR_BIOME(CommandGroup.WORLD, "tileColorBiome"),
    WORLD_TILE_COLOR_COMPOSITE(CommandGroup.WORLD, "tileColorComposite"),
    WORLD_TILE_OVERLAY_CONTINENTS(CommandGroup.WORLD, "tileOverlayContinents"),
    WORLD_TILE_OVERLAY_CHUNKS(CommandGroup.WORLD, "tileOverlayChunks");

    private final CommandGroup group;
    private final String name;

    Command(CommandGroup group, String name) {
        this.group = group;
        this.name = name;
        group.addCommand(this);
    }

    public CommandGroup getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
