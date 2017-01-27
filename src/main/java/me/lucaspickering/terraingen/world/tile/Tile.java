package me.lucaspickering.terraingen.world.tile;

import java.awt.Color;
import java.util.Objects;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.World;

public class Tile {

    public static final int NUM_SIDES = Direction.values().length;

    private static final String INFO_STRING = "Biome: %s%nElevation: %d";
    private static final String DEBUG_INFO_STRING = "Pos: %s%nColor: %s";

    /**
     * The position of this tile within the world. Non-null.
     */
    private final TilePoint pos;

    // Terrain features
    private Biome biome = Biome.NONE;
    private int elevation;

    public Tile(TilePoint pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
    }

    protected Tile(TilePoint pos, Biome biome, int elevation) {
        this(pos);
        Objects.requireNonNull(biome);
        this.biome = biome;
        this.elevation = elevation;
    }

    public final TilePoint pos() {
        return pos;
    }

    /**
     * Is the given tile adjacent to this tile? Two tiles are adjacent if the distance between them
     * is exactly 1.
     *
     * @param tile the other tile (non-null)
     * @return true if this tile and the other are adjacent, false otherwise
     * @throws NullPointerException if {@code tile == null}
     */
    public final boolean isAdjacentTo(Tile tile) {
        Objects.requireNonNull(tile);
        return pos.distanceTo(tile.pos()) == 1;
    }

    public final Biome biome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        Objects.requireNonNull(biome);
        this.biome = biome;
    }

    public final int elevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        // Coerce the elevation to be a valid value
        this.elevation = World.ELEVATION_RANGE.coerce(elevation);
    }

    public final Color backgroundColor() {
        return biome.color(elevation);
    }

    public String info() {
        // If in debug mode, display extra debug info
        if (TerrainGen.instance().getDebug()) {
            final Color bgColor = backgroundColor();
            return String.format(INFO_STRING + "%n" + DEBUG_INFO_STRING,
                                 biome.displayName(), elevation, pos, bgColor);
        }
        return String.format(INFO_STRING, biome, elevation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Tile)) {
            return false;
        }

        final Tile tile = (Tile) o;
        return Objects.equals(pos, tile.pos)
               && Objects.equals(biome, tile.biome)
               && Objects.equals(elevation, tile.elevation);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public String toString() {
        return "Tile@" + pos.toString();
    }

}
