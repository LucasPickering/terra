package me.lucaspickering.terraingen.world;

import java.awt.Color;
import java.util.Objects;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.utils.range.DoubleRange;

public class Tile {

    public static final int NUM_SIDES = Direction.values().length;

    private static final String INFO_STRING =
        "Biome: %s%nElevation: %d%nHumidity: %d%%";
    private static final String DEBUG_INFO_STRING = "%nPos: %s";

    /**
     * An immutable version of a tile. Should be created externally via {@link #immutableCopy()}.
     */
    private static class ImmutableTile extends Tile {

        private ImmutableTile(Tile tile) {
            super(tile.pos(), tile.biome(), tile.elevation(), tile.humidity());
        }

        @Override
        public void setBiome(Biome biome) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setElevation(int elevation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHumidity(double humidity) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The position of this tile within the world. Non-null.
     */
    private final TilePoint pos;
    // Terrain features
    private Biome biome = Biome.NONE;

    private int elevation;
    private double humidity;

    public Tile(TilePoint pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
    }

    private Tile(TilePoint pos, Biome biome, int elevation, double humidity) {
        this(pos);
        Objects.requireNonNull(biome);
        this.biome = biome;
        this.elevation = elevation;
        this.humidity = humidity;
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

    public final double humidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        // Coerce the elevation to be a valid value
        this.humidity = World.HUMIDITY_RANGE.coerce(humidity);
    }

    public final Color getColor(TileColorMode colorMode) {
        switch (colorMode) {
            case ELEVATION:
                return colorMode.interpolateColor(elevation(), World.ELEVATION_RANGE);
            case HUMIDITY:
                // Water tiles are always blue
                if (biome().isWater()) {
                    return Color.BLUE;
                }
                return colorMode.interpolateColor(humidity(), World.HUMIDITY_RANGE);
            case BIOME:
                return biome().color();
            case COMPOSITE:
                final Color elevColor = getColor(TileColorMode.ELEVATION);
                double elevValue = Funcs.toHSV(elevColor)[2];
                elevValue = new DoubleRange(0.25, 1.0).denormalize(elevValue);

                final float[] biomeHsv = Funcs.toHSV(getColor(TileColorMode.BIOME));
                biomeHsv[2] *= (float) elevValue;

                return Funcs.toRGB(biomeHsv);
        }
        throw new IllegalArgumentException("Unknown color mode: " + colorMode);
    }

    public String info() {
        // If in debug mode, display extra debug info
        final String info = String.format(INFO_STRING, biome.displayName(), elevation(),
                                          (int) (humidity() * 100));
        if (TerrainGen.instance().getDebug()) {
            return info + String.format(DEBUG_INFO_STRING, pos);
        }
        return info;
    }

    public Tile immutableCopy() {
        return new ImmutableTile(this);
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
