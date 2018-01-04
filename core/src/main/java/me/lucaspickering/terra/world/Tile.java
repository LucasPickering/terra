package me.lucaspickering.terra.world;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointable;

public class Tile implements HexPointable {

    public static final int NUM_SIDES = Direction.values().length;

    private static final String INFO_STRING = "Biome: %s%n" +
                                              "Elevation: %d%n" +
                                              "Humidity: %d%%";
    private static final String DEBUG_INFO_STRING = "%nPos: %s%n" +
                                                    "Chunk: %s%n" +
                                                    "Water: %.2f|%.2f%n" +
                                                    "River entries: %s%n" +
                                                    "River exits: %s";

    public enum RiverConnection {
        ENTRY, EXIT
    }

    private final HexPoint pos; // The position of this tile in the world (NOT chunk-relative)

    private Chunk chunk; // The chunk that this tile belongs to
    private Continent continent; // The continent that this tile belongs to

    private Biome biome = Biome.NONE;

    private int elevation;
    private double humidity;

    private double waterLevel;
    private double totalWaterTraversed;

    private final Map<Direction, RiverConnection> riverConnections = new EnumMap<>(Direction.class);

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

    public double getWaterLevel() {
        return waterLevel;
    }

    /**
     * Gets the elevation of the top of the water on this tile. In other words, the elevation of
     * this tile plus the water level.
     *
     * @return elevation plus water level
     */
    public double getWaterElevation() {
        return elevation + waterLevel;
    }

    /**
     * Adds the given amount of water to this tile. If this tile is a water biome, (e.g. ocean), no
     * water is added. This also adds to the total amount of water that has traversed this tile.
     *
     * @param water the amount of water to add (non-negative)
     * @return the amount of water added
     * @throws IllegalArgumentException if {@code water} is negative
     */
    public double addWater(double water) {
        if (water < 0.0) {
            throw new IllegalArgumentException(String.format("Water must be positive, was [%f]",
                                                             water));
        }
        if (!biome.isWater()) {
            waterLevel += water;
            totalWaterTraversed += water;
            return water;
        }
        return 0.0;
    }

    /**
     * Removes the given amount of water from this tile. If there isn't enough water on this tile to
     * remove the requested amount, the water level is reduced to zero.
     *
     * @param water the amount of water to remove (non-negative)
     * @return the amount of water removed
     * @throws IllegalArgumentException if {@code water} is negative
     */
    public double removeWater(double water) {
        if (water < 0.0) {
            throw new IllegalArgumentException(String.format("Water must be positive, was [%f]",
                                                             water));
        }
        final double toRemove = Math.min(waterLevel, water);
        waterLevel -= toRemove;
        return toRemove;
    }

    /**
     * Sets this tile's water level to zero.
     *
     * @return the amount of water removed from this tile (i.e. its previous water level)
     */
    public double clearWater() {
        final double remove = waterLevel;
        waterLevel = 0.0;
        return remove;
    }

    public double getWaterTraversed() {
        return totalWaterTraversed;
    }

    public RiverConnection getRiverConnection(Direction dir) {
        return riverConnections.get(dir);
    }

    public void addRiverConnection(Direction dir, RiverConnection conn) {
        riverConnections.put(dir, conn);
    }

    public void removeRiverConnection(Direction dir) {
        riverConnections.remove(dir);
    }

    public String info(boolean debug) {
        // If in debug mode, display extra debug info
        final String info = String.format(INFO_STRING, biome.displayName(), elevation(),
                                          (int) (humidity() * 100));
        if (debug) {
            final StringBuilder entriesString = new StringBuilder();
            final StringBuilder exitsString = new StringBuilder();
            for (Map.Entry<Direction, RiverConnection> entry : riverConnections.entrySet()) {
                switch (entry.getValue()) {
                    case ENTRY:
                        entriesString.append(entry.getKey());
                        entriesString.append(" ");
                        break;
                    case EXIT:
                        exitsString.append(entry.getKey());
                        exitsString.append(" ");
                        break;
                }
            }
            return info + String.format(DEBUG_INFO_STRING, pos, chunk.getPos(),
                                        waterLevel, totalWaterTraversed,
                                        entriesString, exitsString);
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
