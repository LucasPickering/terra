package me.lucaspickering.terraingen.render.screen;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.world.TileColorMode;
import me.lucaspickering.terraingen.world.util.Chunk;
import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.utils.Point;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

class WorldScreenHelper {

    enum TileOverlay {
        NONE, CONTINENT, CHUNK
    }

    static final double TILE_WIDTH = 20.0; // Width of each tile, in pixels
    static final double TILE_HEIGHT = Math.sqrt(3) * TILE_WIDTH / 2.0;
    static final Point[] TILE_VERTICES = {
        new Point(-TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-left
        new Point(+TILE_WIDTH / 4, -TILE_HEIGHT / 2), // Top-right
        new Point(+TILE_WIDTH / 2, 0),                // Right
        new Point(+TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-right
        new Point(-TILE_WIDTH / 4, +TILE_HEIGHT / 2), // Bottom-left
        new Point(-TILE_WIDTH / 2, 0)                 // Left
    };

    static final Range<Double> VALID_WORLD_SCALES = new DoubleRange(0.5, 10.0);

    static final Point SCREEN_CENTER = new Point(Renderer.RES_WIDTH / 2,
                                                 Renderer.RES_HEIGHT / 2);

    // Maximum time a click can be held down to be considered a click and not a drag
    static final int MAX_CLICK_TIME = 250;

    // Change of tile size in pixels with each zoom level
    static final double ZOOM_STEP = 1.0;
    // Each side of the tile is rendered by forming a triangle between it and the center, so
    // there's three vertices for each side of the tile.
    static final int NUM_VERTICES = WorldScreenHelper.TILE_VERTICES.length;
    static final int VERTEX_SIZE = 2;
    static final int COLOR_SIZE = 3; // RGB

    static final int COLOR_SIZE_BYTES = COLOR_SIZE * Float.BYTES;

    // Bind a key to each tile color mode
    static final Map<Integer, TileColorMode> KEY_TO_TILE_COLOR_MODE =
        new HashMap<Integer, TileColorMode>() {{
            put(GLFW.GLFW_KEY_F1, TileColorMode.ELEVATION);
            put(GLFW.GLFW_KEY_F2, TileColorMode.HUMIDITY);
            put(GLFW.GLFW_KEY_F3, TileColorMode.BIOME);
            put(GLFW.GLFW_KEY_F4, TileColorMode.COMPOSITE);
        }};

    // Assign a key to each tile overlay
    static final Map<Integer, TileOverlay> KEY_TO_TILE_OVERLAY =
        new HashMap<Integer, TileOverlay>() {{
            put(GLFW.GLFW_KEY_F5, TileOverlay.CONTINENT);
            put(GLFW.GLFW_KEY_F6, TileOverlay.CHUNK);
        }};

    /**
     * Converts a {@link HexPoint} in to a {@link Point} on the screen.
     *
     * @param tilePos the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    static Point tileToPixel(@NotNull HexPoint tilePos) {
        final double x = TILE_WIDTH * tilePos.x() * 0.75;
        final double y = -TILE_HEIGHT * (tilePos.x() / 2.0 + tilePos.y());
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
    static HexPoint pixelToTile(@NotNull Point pos) {
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
    static Point chunkToPixel(@NotNull HexPoint chunkPos) {
        return tileToPixel(Chunk.getChunkOrigin(chunkPos));
    }
}
