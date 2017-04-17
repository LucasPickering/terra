package me.lucaspickering.terraingen.render.screen;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.utils.Point;

public class WorldScreenHelper {

    public static final double TILE_WIDTH = 20.0; // Width of each tile, in pixels
    public static final double TILE_HEIGHT = (float) Math.sqrt(3) * TILE_WIDTH / 2.0;
    public static final Point[] TILE_VERTICES = {
        new Point(-TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-left
        new Point(+TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-right
        new Point(+TILE_WIDTH / 2, 0),                // Right
        new Point(+TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-right
        new Point(-TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-left
        new Point(-TILE_WIDTH / 2, 0)                 // Left
    };

    /**
     * Converts a {@link HexPoint} in to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public static Point tileToPixel(@NotNull HexPoint tile) {
        final double x = TILE_WIDTH * tile.x() * 0.75;
        final double y = -TILE_HEIGHT * (tile.x() / 2.0 + tile.y());
        return new Point(x, y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link HexPoint}. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there. The given point needs to be shifted and scaled to be in the proper coordinate
     * system before being passed to this function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public static HexPoint pixelToTile(@NotNull Point pos) {
        // Convert it to a fractional tile point
        final double fracX = pos.x() * 4.0 / 3.0 / TILE_WIDTH;
        final double fracY = -(pos.x() + Math.sqrt(3.0) * pos.y())
                             / (TILE_WIDTH * 1.5);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return HexPoint.roundPoint(fracX, fracY, fracZ);
    }
}
