package me.lucaspickering.terra.render.screen;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import me.lucaspickering.terra.render.VertexBufferObject;
import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.utils.Point;

public class ChunkVbo {

    private static final float RIVER_LINE_WIDTH = 3f;

    // All chunks can share the same vertex buffer (only color has to vary between them)
    private static int tilesVertexHandle = -1; // yaay sentinel values

    // These two arrays specify to GL when to start/stop each polygon. The first array tells it
    // the index of the first vertex for each polygon, and the second tells it how many vertices
    // are in each polygon.
    private static final int[] startingIndices = new int[Chunk.TOTAL_TILES];
    private static final int[] sizes = new int[Chunk.TOTAL_TILES];

    static {
        // Populate the two arrays
        for (int i = 0; i < Chunk.TOTAL_TILES; i++) {
            startingIndices[i] = i * WorldScreenHelper.NUM_VERTICES;
            sizes[i] = WorldScreenHelper.NUM_VERTICES;
        }
    }

    private final WorldScreen worldScreen;
    private final Chunk chunk;
    private VertexBufferObject tilesVbo;
    private VertexBufferObject riversVbo;

    public ChunkVbo(WorldScreen worldScreen, Chunk chunk) {
        this.worldScreen = worldScreen;
        this.chunk = chunk;
        initTiles();
        initRivers();
    }

    private void initTiles() {
        // Save this chunk's position on the screen
        chunk.setScreenPos(WorldScreenHelper.chunkToPixel(chunk.getPos()));

        // Create a VBO for the chunk
        final VertexBufferObject vbo = new VertexBufferObject.Builder()
            .setNumVertices(WorldScreenHelper.NUM_VERTICES * Chunk.TOTAL_TILES)
            .setDrawFunction(() -> GL14.glMultiDrawArrays(GL11.GL_TRIANGLE_FAN,
                                                          startingIndices, sizes))
            .build();

        // If this is the first chunk VBO to be initialized, we need to create the vertex VBO.
        // Otherwise, use the pre-existing VBO.
        if (tilesVertexHandle == -1) {
            initTileVertices(vbo);
        } else {
            // The vertex VBO was initialized already, just use its handle
            vbo.setVertexHandle(tilesVertexHandle);
        }

        vbo.bindColorBuffer(GL15.GL_DYNAMIC_DRAW); // Bind the color buffer now and populate later

        // Put the handle in the map, then populate the buffer with the correct colors
        tilesVbo = vbo;
        updateColors();
    }

    private void initTileVertices(VertexBufferObject vbo) {
        // Add vertices for each tile in the chunk
        for (int x = 0; x < Chunk.SIDE_LENGTH; x++) {
            for (int y = 0; y < Chunk.SIDE_LENGTH; y++) {
                final Point tileCenter = WorldScreenHelper.tileToPixel(new HexPoint(x, y));
                for (Point vertex : WorldScreenHelper.TILE_VERTICES) {
                    // Shift this vertex by the tile's center, and add it to the VBO
                    vbo.addVertex(tileCenter.plus(vertex));
                }
            }
        }
        vbo.bindVertexBuffer(GL15.GL_STATIC_DRAW);
        tilesVertexHandle = vbo.getVertexHandle();
    }

    private void initRivers() {
        // Populate a list of all river vertices for this chunk
        final List<Point> vertices = new LinkedList<>();
        for (Tile tile : chunk.getTiles()) {
            final Point tileCenter =
                WorldScreenHelper.tileToPixel(Chunk.getRelativeTilePos(tile.pos()));
            for (Direction dir : Direction.values()) {
                if (tile.getRiverConnection(dir) != null) {
                    final Point midpoint = WorldScreenHelper.TILE_SIDE_MIDPOINTS[dir.ordinal()];
                    vertices.add(tileCenter);
                    vertices.add(tileCenter.plus(midpoint));
                }
            }
        }

        // Init a VBO
        final VertexBufferObject vbo = new VertexBufferObject.Builder()
            .setNumVertices(vertices.size())
            .setDrawFunction(() -> {
                GL11.glLineWidth(RIVER_LINE_WIDTH);
                GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.size());
            })
            .build();

        // Populate the vertex and color buffers
        for (Point vertex : vertices) {
            vbo.addVertex(vertex);
            vbo.addColor(Color.BLUE);
        }

        vbo.bindVertexBuffer(GL15.GL_STATIC_DRAW);
        vbo.bindColorBuffer(GL15.GL_STATIC_DRAW);

        riversVbo = vbo;
    }

    /**
     * Updates the color of each tile in this chunk. The color for each tile is recalculated,
     * then updated in the color buffer.
     */
    public void updateColors() {
        // Bind the handle for the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tilesVbo.getColorHandle());
        long offset = 0;
        for (Tile tile : chunk.getTiles()) {
            final Color color = getTileColor(tile);
            for (int i = 0; i < WorldScreenHelper.NUM_VERTICES; i++) {
                offset += tilesVbo.setVertexColor(offset, color);
            }
        }
    }

    /**
     * Gets the color of this tile, including the overlay color. The overlay color is mixed in
     * according to its alpha value.
     *
     * @param tile the tile whose color we are calculating
     * @return the calculated color of the given tile
     */
    private Color getTileColor(Tile tile) {
        final Color baseColor = worldScreen.getTileColorMode().getColor(tile);
        final Color overlayColor = worldScreen.getTileOverlayMode().getColor(tile);

        // If there is an overlay color, mix the two colors
        if (overlayColor != null) {
            return Funcs.overlayColors(overlayColor, baseColor);
        }
        return baseColor;
    }

    /**
     * Draws this chunk, including tiles, overlays, rivers, etc.
     */
    public void draw() {
        // Translate to this chunk's pixel position
        GL11.glPushMatrix();
        GL11.glTranslated(chunk.getScreenPos().x(), chunk.getScreenPos().y(), 0.0);

        tilesVbo.draw();
        riversVbo.draw();

        GL11.glPopMatrix();
    }
}
