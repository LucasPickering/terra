package me.lucaspickering.groundwar.world.tile;

import java.util.Objects;

import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Direction;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.world.Biome;

public class Tile {

    public static final class Builder {

        private final Point pos;
        private Biome biome;
        private int elevation;

        /**
         * Every tile has to have a position, so make it required by the constructor.
         *
         * @param pos the position of this tile
         */
        private Builder(Point pos) {
            // Privet o force usage of fromPos
            this.pos = pos;
        }

        public static Builder fromPos(Point pos) {
            return new Builder(pos);
        }

        public Builder biome(Biome biome) {
            this.biome = biome;
            return this;
        }

        public Builder elevation(int elevation) {
            this.elevation = elevation;
            return this;
        }

        public Tile build() {
            return new Tile(pos, biome, elevation);
        }
    }

    /**
     * The position of this tile within the world. Non-null.
     */
    private final Point pos;
    private final Biome biome;
    private final int elevation;

    /**
     * The position of the top-left corner of the texture of this tile on the screen.
     */
    private final Point screenPos;
    private final Tile[] adjacentTiles = new Tile[Constants.NUM_SIDES];

    private Tile(Point pos, Biome biome, int elevation) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(biome);
        this.pos = pos;
        this.biome = biome;
        this.elevation = elevation;
        this.screenPos = Constants.BOARD_CENTER.plus(
            (int) (Constants.TILE_WIDTH * pos.getX() * 0.75f),
            (int) (-Constants.TILE_HEIGHT * (pos.getX() / 2.0f + pos.getY())));
    }

    public final Tile[] getAdjacentTiles() {
        return adjacentTiles;
    }

    public final Tile getAdjacentTile(Direction dir) {
        return adjacentTiles[dir.ordinal()];
    }

    /**
     * Copies the contents of {@code adjTiles} into {@link #adjacentTiles}.
     *
     * @param adjTiles the array to be copied from
     * @throws NullPointerException     if {@code adjTiles == null}
     * @throws IllegalArgumentException if {@code adjTiles.length != {@link Constants#NUM_SIDES}}
     */
    public final void setAdjacentTiles(Tile[] adjTiles) {
        Objects.requireNonNull(adjTiles);
        if (adjTiles.length != Constants.NUM_SIDES) {
            throw new IllegalArgumentException("I need " + Constants.NUM_SIDES + " sides!");
        }
        System.arraycopy(adjTiles, 0, adjacentTiles, 0, Constants.NUM_SIDES);
        onSetAdjacents();
    }

    public final Point getPos() {
        return pos;
    }

    public final int getElevation() {
        return elevation;
    }

    public final Point getScreenPos() {
        return screenPos;
    }

    public final Point getCenterPos() {
        return screenPos.plus(Constants.TILE_WIDTH / 2, Constants.TILE_HEIGHT / 2);
    }

    public final int getBackgroundColor() {
        return biome.color();
    }

    public final int getOutlineColor() {
        return Colors.TILE_OUTLINE;
    }

    /**
     * Gets the distance between this tile and another tile located at the given point.
     *
     * @param p the other point
     * @return the distance between this tile and {@param p}
     */
    public final int distanceTo(Point p) {
        final int x1 = pos.getX();
        final int y1 = pos.getY();
        final int x2 = p.getX();
        final int y2 = p.getY();
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(-x1 - y1 + x2 + y2)) / 2;
    }

    /**
     * Convenice method for {@link #distanceTo(Point)}.
     *
     * @param tile the other tile
     * @return {@code distanceTo(tile.getPos()}
     */
    public final int distanceTo(Tile tile) {
        return distanceTo(tile.getPos());
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
        return distanceTo(tile) == 1;
    }

    /**
     * Does this tile contain the {@link Point} p? p is a point in screen-space, not in tile-space.
     * This is essentially used to check if the mouse is over this tile.
     *
     * @param p the point
     * @return true if this tile contains p, false otherwise
     */
    public final boolean contains(Point p) {
        return getCenterPos().distanceTo(p) <= Constants.TILE_RADIUS;
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

    // Events

    /**
     * Called <i>directly after</i> {@link #adjacentTiles} is populated.
     */
    public void onSetAdjacents() {
    }
}
