package me.lucaspickering.terra.render.screen;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.utils.Point2;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

class WorldScreenHelper {

    static final double TILE_RADIUS = 10.0; // Distance from the center of the tile to each VERTEX
    static final double TILE_WIDTH = TILE_RADIUS * 2; // Width of each tile, in pixels
    static final double TILE_HEIGHT = Math.sqrt(3) * TILE_RADIUS;
    static final Point2[] TILE_VERTICES = {
        new Point2(-TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-left
        new Point2(+TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-right
        new Point2(+TILE_WIDTH / 2, 0),                // Right
        new Point2(+TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-right
        new Point2(-TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-left
        new Point2(-TILE_WIDTH / 2, 0)                 // Left
    };
    static final Point2[] TILE_SIDE_MIDPOINTS = {
        new Point2(0, -TILE_HEIGHT / 2),                         // North
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, -TILE_HEIGHT / 4), // Northeast
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, +TILE_HEIGHT / 4), // Southest
        new Point2(0, TILE_HEIGHT / 2),                          // South
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, +TILE_HEIGHT / 4), // Southwest
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, -TILE_HEIGHT / 4)  // Northwest
    };

    static final Range<Double> VALID_WORLD_SCALES = new DoubleRange(0.5, 10.0);

    static final Point2 SCREEN_CENTER = new Point2(Renderer.RES_WIDTH / 2,
                                                   Renderer.RES_HEIGHT / 2);

    // Maximum time a click can be held down to be considered a click and not a drag
    static final int MAX_CLICK_TIME = 250;

    // Change of tile size in pixels with each zoom level
    static final double ZOOM_STEP = 1.0;

    static final int NUM_VERTICES = WorldScreenHelper.TILE_VERTICES.length;

    /**
     * Converts a {@link HexPoint} in to a point on the screen.
     *
     * @param tilePos the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    static Point2 tileToPixel(@NotNull HexPoint tilePos) {
        final double x = TILE_WIDTH * tilePos.x() * 0.75;
        final double y = -TILE_HEIGHT * (tilePos.x() / 2.0 + tilePos.y());
        return new Point2(x, y);
    }

    /**
     * Converts a point on the screen to a {@link HexPoint}. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there. The given point needs to be shifted and scaled to be in the proper coordinate
     * system before being passed to this function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    static HexPoint pixelToTile(@NotNull Point2 pos) {
        // Convert it to a fractional tile point
        final double fracX = pos.x() * 4.0 / 3.0 / TILE_WIDTH;
        final double fracY = -(pos.x() + Math.sqrt(3.0) * pos.y())
                             / (TILE_WIDTH * 1.5);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return HexPoint.roundPoint(fracX, fracY, fracZ);
    }

    /**
     * Gets the pixel position of the center of the tile at the bottom-left of the given chunk.
     *
     * @param chunkPos the chunk
     * @return the center of the tile at the bottom-left of the chunk
     */
    @NotNull
    static Point2 chunkToPixel(@NotNull HexPoint chunkPos) {
        return tileToPixel(Chunk.getChunkOrigin(chunkPos));
    }
}
