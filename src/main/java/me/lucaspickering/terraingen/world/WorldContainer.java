package me.lucaspickering.terraingen.world;

import java.util.List;

public class WorldContainer {

    private final Tiles tiles;
    private List<Cluster> continents;

    public WorldContainer(int radius) {
        tiles = Tiles.initByRadius(radius);
    }

    /**
     * Copy constructor
     */
    private WorldContainer(Tiles tiles, List<Cluster> continents) {
        this.tiles = tiles;
        this.continents = continents;
    }

    public Tiles getTiles() {
        return tiles;
    }

    public List<Cluster> getContinents() {
        return continents;
    }

    public void setContinents(List<Cluster> continents) {
        this.continents = continents;
    }

    public WorldContainer immutableCopy() {
        // TODO Make continents immutable in this
        return new WorldContainer(tiles.immutableCopy(), continents);
    }
}
