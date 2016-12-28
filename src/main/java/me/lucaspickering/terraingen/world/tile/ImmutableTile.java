package me.lucaspickering.terraingen.world.tile;

import java.util.Map;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.world.Biome;

public class ImmutableTile extends Tile {

    public ImmutableTile(Tile tile) {
        super(tile.pos(), tile.biome(), tile.elevation());
        super.setAdjacents(tile.adjacents());
    }

    @Override
    public void setAdjacents(Map<Direction, Tile> adjacents) {
        throw new UnsupportedOperationException();
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
