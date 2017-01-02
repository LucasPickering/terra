package me.lucaspickering.terraingen.world.tile;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.WorldHelper;

public class Tile {

    public static final int NUM_SIDES = Direction.values().length;

    // UI constants
    /**
     * The distance between the center point of the hexagon and each vertex. Also the length of
     * one side of the tile.
     */
    public static final int RADIUS = 37;

    /**
     * Distance in pixels from the left-most vertex to the right-most vertex.
     */
    public static final int WIDTH = RADIUS * 2;

    /**
     * Distance in pixels from the top side to the bottom side.
     */
    public static final int HEIGHT = (int) (Math.sqrt(3) * RADIUS);

    /**
     * An array of coordinates referring to each vertex of a tile, with coordinates being relative
     * to the center of the tile. The first vertex is the top-left, and they move clockwise from
     * there.
     */
    public static final Point[] VERTICES = new Point[]{
        new Point(-WIDTH / 4, -HEIGHT / 2),
        new Point(WIDTH / 4, -HEIGHT / 2),
        new Point(RADIUS, 0),
        new Point(WIDTH / 4, HEIGHT / 2),
        new Point(-WIDTH / 4, HEIGHT / 2),
        new Point(-RADIUS, 0)
    };

    private static final String INFO_STRING = "Biome: %s%nElevation: %d";
    private static final String DEBUG_INFO_STRING = "Pos: %s%nColor: %s";

    /**
     * The position of this tile within the world. Non-null.
     */
    private final TilePoint pos;
    private Map<Direction, Tile> adjacents;

    // Terrain features
    private Biome biome = Biome.NONE;
    private int elevation;

    /**
     * Pixel position of the center of the texture of this tile, relative to the origin tile.
     * This needs to be shifted by the world center
     * ({@link me.lucaspickering.terraingen.world.World#getWorldCenter}) before being drawn on
     * screen.
     */
    private final Point center;
    private final Point topLeft;
    private final Point topRight;
    private final Point bottomRight;
    private final Point bottomLeft;

    public Tile(TilePoint pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
        this.center = WorldHelper.tileToPixel(pos);
        topLeft = center.plus(-WIDTH / 2, -HEIGHT / 2);
        topRight = center.plus(WIDTH / 2, -HEIGHT / 2);
        bottomRight = center.plus(WIDTH / 2, HEIGHT / 2);
        bottomLeft = center.plus(-WIDTH / 2, HEIGHT / 2);
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
     * Gets the set of tiles adjacent to this one. The returned {@link Map} will be unmodifiable.
     *
     * @return the tiles adjacent to this one
     * @throws IllegalStateException if this tile's adjacent tiles has not yet been set
     */
    public final Map<Direction, Tile> adjacents() {
        if (adjacents == null) {
            throw new IllegalStateException("Map of adjacent tiles has not yet been initialized");
        }
        return adjacents;
    }

    /**
     * Sets the set of tiles adjacent to this one. An unmodifiable map will be created, backed by
     * the given map, and that will be passed out to callers of {@link #adjacents()}. Any changes to
     * the map passed to this function will be reflected in this tile's adjacents map so throw the
     * given map away after calling this function!
     *
     * @param adjacents the tiles adjacent to this one
     * @throws NullPointerException  if {@code adjacents == null}
     * @throws IllegalStateException if this tile's adjacent tiles has already been set
     */
    public void setAdjacents(Map<Direction, Tile> adjacents) {
        if (this.adjacents != null) {
            throw new IllegalStateException("Map of adjacent tiles has already been initialized");
        }
        Objects.requireNonNull(adjacents);
        this.adjacents = Collections.unmodifiableMap(adjacents);
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

    public final Point getCenter() {
        return center;
    }

    public final Point getTopLeft() {
        return topLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public final Color backgroundColor() {
        return biome.color(elevation);
    }

    public final Color outlineColor() {
        return Colors.TILE_OUTLINE;
    }

    public String info() {
        // If in debug mode, display extra debug info
        if (TerrainGen.instance().debug()) {
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
