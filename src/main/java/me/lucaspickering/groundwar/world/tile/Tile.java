package me.lucaspickering.groundwar.world.tile;

import java.util.Objects;

import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.Biome;

public class Tile {

    public static final class Builder {

        private final TilePoint pos;
        private Biome biome;
        private int elevation;

        /**
         * Every tile has to have a position, so make it required by the constructor.
         *
         * @param pos the position of this tile
         */
        private Builder(TilePoint pos) {
            // Privet o force usage of fromPos
            this.pos = pos;
        }

        public static Builder fromPos(TilePoint pos) {
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
    private final TilePoint pos;
    private final Biome biome;
    private final int elevation;

    /**
     * The position of the top-left corner of the texture of this tile on the screen.
     */
    private final Point screenPos;

    private Tile(TilePoint pos, Biome biome, int elevation) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(biome);
        this.pos = pos;
        this.biome = biome;
        this.elevation = elevation;
        this.screenPos = Constants.BOARD_CENTER.plus(
            (int) (Constants.TILE_WIDTH * pos.x() * 0.75f),
            (int) (-Constants.TILE_HEIGHT * (pos.x() / 2.0f + pos.y())));
    }

    public final TilePoint pos() {
        return pos;
    }

    public final int elevation() {
        return elevation;
    }

    public final Point screenPos() {
        return screenPos;
    }

    public final Point centerPOs() {
        return screenPos.plus(Constants.TILE_WIDTH / 2, Constants.TILE_HEIGHT / 2);
    }

    public final int backgroundColor() {
        return biome.color();
    }

    public final int outlineColor() {
        return Colors.TILE_OUTLINE;
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
     * Does this tile contain the {@link Point} p? p is a point in screen-space, not in tile-space.
     * This is essentially used to check if the mouse is over this tile.
     *
     * @param p the point
     * @return true if this tile contains p, false otherwise
     */
    public final boolean contains(Point p) {
        return centerPOs().distanceTo(p) <= Constants.TILE_RADIUS;
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
