package me.lucaspickering.groundwar.world.tile;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Direction;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.Biome;
import me.lucaspickering.groundwar.world.WorldHelper;

public class Tile {

    public static final class Builder {

        private final TilePoint pos;
        private Map<Direction, Tile.Builder> adjacents;
        private Biome biome;
        private int elevation;

        /**
         * Every tile has to have a position, so make it required by the constructor.
         *
         * @param pos the position of this tile
         */
        public Builder(TilePoint pos) {
            this.pos = pos;
        }

        public Biome getBiome() {
            return biome;
        }

        public Builder setBiome(Biome biome) {
            this.biome = biome;
            return this;
        }

        public int getElevation() {
            return elevation;
        }

        public Builder setElevation(int elevation) {
            this.elevation = elevation;
            return this;
        }

        public final Map<Direction, Tile.Builder> adjacents() {
            return adjacents;
        }

        public final void setAdjacents(Map<Direction, Tile.Builder> adjacents) {
            this.adjacents = Collections.unmodifiableMap(adjacents);
        }

        public Tile build() {
            return new Tile(pos, biome, elevation);
        }
    }

    public static final int NUM_SIDES = Direction.values().length;

    // UI constants
    /**
     * The distance between the center point of the hexagon and each vertex. Also the length of
     * one side of the tile.
     */
    public static final int RADIUS = 74;

    /**
     * Distance in pixels from the left-most vertex to the right-most vertex.
     */
    public static final int WIDTH = RADIUS * 2;

    /**
     * Distance in pixels from the top side to the bottom side.
     */
    public static final int HEIGHT = (int) (Math.sqrt(3) * RADIUS);

    /**
     * The width of the lines drawn to outline the tile.
     */
    public static final float OUTLINE_WIDTH = 1.5f;

    /**
     * An array of coordinates referring to each vertex of a tile, with coordinates being
     * relative to the center of the tile. The first vertex is the top-left, and they move
     * clockwise from there.
     */
    public static final Point[] VERTICES = new Point[]{
        new Point(WIDTH / 4, 0),
        new Point(WIDTH * 3 / 4, 0),
        new Point(WIDTH, HEIGHT / 2),
        new Point(WIDTH * 3 / 4, HEIGHT),
        new Point(WIDTH / 4, HEIGHT),
        new Point(0, HEIGHT / 2)
    };

    private static final String INFO_STRING = "Pos: %s%nBiome: %s%nElevation: %d";

    /**
     * The position of this tile within the world. Non-null.
     */
    private final TilePoint pos;
    private Map<Direction, Tile> adjacents;

    // Terrain features
    private final Biome biome;
    private final int elevation;

    /**
     * The position of the top-left corner of the texture of this tile on the screen.
     */
    private final Point center;
    private final Point topLeft;

    private Tile(TilePoint pos, Biome biome, int elevation) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(biome);
        this.pos = pos;
        this.biome = biome;
        this.elevation = elevation;
        this.center = WorldHelper.tileToPixel(pos);
        this.topLeft = center.minus(WIDTH / 2, HEIGHT / 2);
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
     * the given map, and that will be passed out to callers of {@link #adjacents()}. Any changes
     * to the map passed to this function will be reflected in this tile's adjacents map so throw
     * the given map away after calling this function!
     *
     * @param adjacents the tiles adjacent to this one
     * @throws NullPointerException  if {@code adjacents == null}
     * @throws IllegalStateException if this tile's adjacent tiles has already been set
     */
    public final void setAdjacents(Map<Direction, Tile> adjacents) {
        if (this.adjacents != null) {
            throw new IllegalStateException("Map of adjacent tiles has already been initialized");
        }
        Objects.requireNonNull(adjacents);
        this.adjacents = Collections.unmodifiableMap(adjacents);
    }

    public final int elevation() {
        return elevation;
    }

    public final Point center() {
        return center;
    }

    public final Point topLeft() {
        return topLeft;
    }

    public final Color backgroundColor() {
        return biome.color();
    }

    /**
     * Get the color of the side of the outline in the given direction. Each side of the outline
     * can have its own color.
     *
     * @param dir the direction of the side to get the color for
     * @return the color of the request side
     */
    public final Color outlineColor(Direction dir) {
        return Colors.TILE_OUTLINE;
    }

    public String info() {
        return String.format(INFO_STRING, pos, biome, elevation);
    }

    /**
     * Is the given tile adjacent to this tile? Two tiles are adjacent if the distance between them
     * is exactly 1.
     *
     * @param tile the other tile (non-null)
     * @return true if this tile and the other are adjacent, false otherwise
     * @throws NullPointerException if {@code tile == null}
     */
    public boolean isAdjacentTo(Tile tile) {
        Objects.requireNonNull(tile);
        return pos.distanceTo(tile.pos()) == 1;
    }

    /**
     * Does this tile contain the given {@link Point} p? p is a point in screen-space, not in
     * tile-space.
     *
     * This is used to check if the mouse is over.
     *
     * @param p the point
     * @return true if this tile contains p, false otherwise
     */
    public final boolean contains(Point p) {
        // Convert the given pixel location to a tile point, then check if that point is
        return pos.equals(WorldHelper.pixelToTile(p));
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
        return Objects.equals(pos, tile.pos);
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
