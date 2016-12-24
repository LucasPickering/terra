package me.lucaspickering.terraingen.world;

public enum Biome {

    OCEAN("Ocean", 0.7f),
    PLAINS("Plains", 0.3f),
    MOUNTAIN("Mountain", 0.4f);

    private final String displayName;
    private final float hue;

    Biome(String displayName, float hue) {
        this.displayName = displayName;
        this.hue = hue;
    }

    public String displayName() {
        return displayName;
    }

    public float hue() {
        return hue;
    }
}
