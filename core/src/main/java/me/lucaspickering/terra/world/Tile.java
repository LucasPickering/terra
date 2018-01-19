package me.lucaspickering.terra.world;

import java.util.Objects;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointable;
import me.lucaspickering.terra.world.util.RunoffPattern;

public class Tile implements HexPointable {

    public static final int NUM_SIDES = Direction.values().length;

    private static final String INFO_STRING = "Biome: %s%n" +
                                              "Elevation: %d%n" +
                                              "Humidity: %d%%";
    private static final String DEBUG_INFO_STRING = "%nPos: %s%n" +
                                                    "Chunk: %s%n" +
                                                    "Water: %.2f|%.2f%n";

    private final HexPoint pos; // The position of this tile in the world (NOT chunk-relative)

    private Chunk chunk; // The chunk that this tile belongs to
    private Continent continent; // The continent that this tile belongs to

    private Biome biome = Biome.NONE;

    private int elevation;
    private double humidity;

    private double runoffLevel;
    private double totalRunoffTraversed;

    private final RunoffPattern runoffPattern = new RunoffPattern(this);

    public Tile(HexPoint pos, Chunk chunk) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(chunk);
        this.pos = pos;
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        Objects.requireNonNull(chunk);
        this.chunk = chunk;
    }

    public Continent getContinent() {
        return continent;
    }

    /**
     * Sets this tile's continent. The caller of this function must always make sure that this tile
     * is added to the given continent, so that this tile's continent field stays consistent with
     * that continent's container. It is also the caller's responsibility to ensure that, if this
     * tile previously belonged to another continent, then it is removed from that continent.
     *
     * @param continent the new continent for this tile to belong to
     */
    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public final HexPoint pos() {
        return pos;
    }

    @Override
    public final HexPoint toHexPoint() {
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

    public double getRunoffLevel() {
        return runoffLevel;
    }

    /**
     * Adds the given amount of runoff to this tile.
     *
     * @param runoff the amount of runoff to add (non-negative)
     * @return the amount of runoff added
     * @throws IllegalArgumentException if {@code runoff} is negative
     */
    public double addRunoff(double runoff) {
        if (runoff < 0.0) {
            throw new IllegalArgumentException(String.format("Runoff must be positive, was [%f]",
                                                             runoff));
        }

        // Onnly land tiles can take runoff - water tiles swallow it and move on with their days
        if (biome.isLand()) {
            runoffLevel += runoff;
            return runoff;
        }
        return 0.0;
    }

    /**
     * Sets this tile's runoff level to zero.
     *
     * @return the amount of runoff removed from this tile (i.e. its previous runoff level)
     */
    public double clearRunoff() {
        final double remove = runoffLevel;
        runoffLevel = 0.0;
        return remove;
    }

    public double getRunoffTraversed() {
        return totalRunoffTraversed;
    }

    public void addRunoffTraversed(double traversed) {
        if (traversed < 0.0) {
            throw new IllegalArgumentException(String.format(
                "Runoff traversed must be positive, was [%f]", traversed));
        }
        totalRunoffTraversed += traversed;
    }

    public RunoffPattern getRunoffPattern() {
        return runoffPattern;
    }

    public String info(boolean debug) {
        // If in debug mode, display extra debug info
        final String info = String.format(INFO_STRING, biome.displayName(), elevation(),
                                          (int) (humidity() * 100));
        if (debug) {
            return info + String.format(DEBUG_INFO_STRING, pos, chunk.getPos(),
                                        runoffLevel, totalRunoffTraversed);
        }
        return info;
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
