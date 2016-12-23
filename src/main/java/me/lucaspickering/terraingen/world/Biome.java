package me.lucaspickering.terraingen.world;

import me.lucaspickering.terraingen.util.Colors;

public enum Biome {

    PLAINS("Plains") {
        @Override
        public Colors.HSVColor color(int elevation) {
            return new Colors.HSVColor(0.3f, 1f, 1f - (float) elevation / World.MAX_ELEVATION);
        }
    },
    MOUNTAIN("Mountain") {
        @Override
        public Colors.HSVColor color(int elevation) {
            return new Colors.HSVColor(0f, 0f, 1f - (float) elevation / World.MAX_ELEVATION);
        }
    };

    private final String displayName;

    Biome(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public abstract Colors.HSVColor color(int elevation);
}
