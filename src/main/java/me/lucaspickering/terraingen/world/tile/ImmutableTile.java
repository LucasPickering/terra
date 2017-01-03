package me.lucaspickering.terraingen.world.tile;

import me.lucaspickering.terraingen.world.Biome;

public class ImmutableTile extends Tile {

    public ImmutableTile(Tile tile) {
        super(tile.pos(), tile.biome(), tile.elevation());
    }

    @Override
    public void setBiome(Biome biome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setElevation(int elevation) {
        throw new UnsupportedOperationException();
    }
}
