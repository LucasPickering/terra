package me.lucaspickering.terra.render.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import me.lucaspickering.terra.render.VertexBufferObject;
import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.utils.Point2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glMultiDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;

public class ChunkVbo {

    private static final int PRIMITIVE_RESTART_INDEX = -1;
    private static final float RIVER_LINE_WIDTH = 1.5f;

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
        final VertexBufferObject.Builder vboBuilder = new VertexBufferObject.Builder()
            .colorUsage(GL_DYNAMIC_DRAW);

        // Add vertices for each tile in the chunk
        for (int x = 0; x < Chunk.SIDE_LENGTH; x++) {
            for (int y = 0; y < Chunk.SIDE_LENGTH; y++) {
                addTileVertices(vboBuilder, new HexPoint(x, y));
            }
        }

        // Set draw function
        final int numVertices = vboBuilder.getNumVertices();
        vboBuilder.drawFunction(() -> {
            glMultiDrawArrays(GL_TRIANGLE_FAN, startingIndices, sizes);
        });

        tilesVbo = vboBuilder.build(); // Build the VBO
        updateColors(); // Populate the color buffer with the correct colors
    }

    private void addTileVertices(VertexBufferObject.Builder vboBuilder, HexPoint tilePos) {
        final Point2 tileCenter = WorldScreenHelper.tileToPixel(tilePos);
        for (Point2 vertex : WorldScreenHelper.TILE_VERTICES) {
            // Add the index of the vertex that is about to be added
            vboBuilder.addIndex(vboBuilder.getNumVertices());

            // Add the vertex with a placeholder color
            vboBuilder.addVertex(tileCenter.plus(vertex), Color.BLACK);
        }
    }

    private void initRivers() {
        // Populate a list of all river vertices for this chunk
        final List<Point2> vertices = new ArrayList<>();
        for (Tile tile : chunk.getTiles()) {
            final Point2 tileCenter =
                WorldScreenHelper.tileToPixel(Chunk.getRelativeTilePos(tile.pos()));
            for (Direction dir : Direction.values()) {
                if (tile.getRiverConnection(dir) != null) {
                    final Point2 midpoint = WorldScreenHelper.TILE_SIDE_MIDPOINTS[dir.ordinal()];
                    vertices.add(tileCenter);
                    vertices.add(tileCenter.plus(midpoint));
                }
            }
        }

        // Init a VBO
        final VertexBufferObject.Builder vboBuilder = new VertexBufferObject.Builder()
            .drawFunction(() -> {
                glLineWidth((float) worldScreen.getWorldScale() * RIVER_LINE_WIDTH);
                glDrawElements(GL_LINES, vertices.size(), GL_UNSIGNED_INT, 0);
            });

        // Populate the vertex, color, and index buffers
        for (int i = 0; i < vertices.size(); i++) {
            vboBuilder.addVertex(vertices.get(i), Color.BLUE);
            vboBuilder.addIndex(i);
        }

        riversVbo = vboBuilder.build();
    }

    /**
     * Updates the color of each tile in this chunk. The color for each tile is recalculated, then
     * updated in the color buffer.
     */
    public void updateColors() {
        // Bind the handle for the color buffer
        glBindBuffer(GL_ARRAY_BUFFER, tilesVbo.getColorHandle());
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
        glPushMatrix();
        glTranslated(chunk.getScreenPos().x(), chunk.getScreenPos().y(), 0.0);

        tilesVbo.draw();
        riversVbo.draw();

        glPopMatrix();
    }
}
