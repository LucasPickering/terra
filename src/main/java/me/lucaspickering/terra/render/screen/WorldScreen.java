package me.lucaspickering.terra.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.ButtonAction;
import me.lucaspickering.terra.input.KeyEvent;
import me.lucaspickering.terra.input.MouseButtonEvent;
import me.lucaspickering.terra.input.ScrollEvent;
import me.lucaspickering.terra.render.Font;
import me.lucaspickering.terra.render.VertexBufferObject;
import me.lucaspickering.terra.render.screen.gui.MouseTextBox;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.WorldHandler;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.utils.Point;

public class WorldScreen extends Screen {

    private final WorldHandler worldHandler;
    private final MouseTextBox mouseOverTileInfo;
    private Point worldCenter; // The pixel location of the center of the world
    private double worldScale = 1.0;

    private TileColorMode tileColorMode = TileColorMode.COMPOSITE;
    private WorldScreenHelper.TileOverlay tileOverlay = WorldScreenHelper.TileOverlay.NONE;

    private Point lastMouseDragPos; // The last position of the mouse while dragging, or null
    private Tile mouseOverTile; // The tile that the mouse is currently over
    private long mouseDownTime; // The time at which the user pressed the mouse button down

    private final Logger logger;

    // CHUNK VBO FIELDS
    private int chunkVertexHandle = -1; // -1 indicates it hasn't been set yet
    private final HexPointMap<Chunk, VertexBufferObject> tileVbos = new HexPointMap<>();
    private final HexPointMap<Chunk, VertexBufferObject> riverVbos = new HexPointMap<>();
    private final int[] startingIndices = new int[Chunk.TOTAL_TILES];
    private final int[] sizes = new int[Chunk.TOTAL_TILES];

    private int hexVertexHandle;
    private VertexBufferObject mouseOverVbo;

    public WorldScreen(WorldHandler worldHandler) {
        Objects.requireNonNull(worldHandler);

        logger = Logger.getLogger(getClass().getName());
        this.worldHandler = worldHandler;
        worldCenter = WorldScreenHelper.SCREEN_CENTER;
        mouseOverTileInfo = new MouseTextBox();
        mouseOverTileInfo.setVisible(false); // Hide this for now
        addGuiElement(mouseOverTileInfo);
        initVbos();
    }

    private void initVbos() {
        // Populate the array that tells GL which vertex to start at for each polygon, and
        // another that tells it how big (in vertices) each polygon is.
        for (int i = 0; i < Chunk.TOTAL_TILES; i++) {
            startingIndices[i] = i * WorldScreenHelper.NUM_VERTICES;
            sizes[i] = WorldScreenHelper.NUM_VERTICES;
        }

        for (Chunk chunk : worldHandler.getWorld().getChunks()) {
            initTileVbo(chunk);
            initRiverVbo(chunk);
        }

        initHexVbos(); // Init single-tile VBOs, such as mouse-over highlight
    }

    private void initTileVbo(Chunk chunk) {
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
        if (chunkVertexHandle == -1) {
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
            chunkVertexHandle = vbo.getVertexHandle();
        } else {
            // The vertex VBO was initialized already, just use its handle
            vbo.setVertexHandle(chunkVertexHandle);
        }

        vbo.bindColorBuffer(GL15.GL_DYNAMIC_DRAW); // Bind the color buffer now and populate later

        // Put the handle in the map, then populate the buffer with the correct colors
        tileVbos.put(chunk, vbo);
        updateChunkColors(chunk, vbo);
    }

    private void initRiverVbo(Chunk chunk) {
        final List<Point> vertices = new LinkedList<>();
        for (Tile tile : chunk.getTiles()) {
            final Point tileCenter =
                WorldScreenHelper.tileToPixel(Chunk.getRelativeTilePos(tile.pos()));
            for (Direction dir : Direction.values()) {
                if (tile.getRiverConnection(dir) != null) {
                    final Point midpoint = WorldScreenHelper.TILE_SIDE_MIDPOINTS[dir.ordinal()];
                    vertices.add(tileCenter.plus(midpoint));
                }
            }
        }

        final VertexBufferObject vbo = new VertexBufferObject.Builder()
            .setNumVertices(vertices.size())
            .setDrawFunction(() -> GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.size()))
            .build();

        for (Point vertex : vertices) {
            vbo.addVertex(vertex);
            vbo.addColor(Color.BLUE);
        }

        vbo.bindVertexBuffer(GL15.GL_STATIC_DRAW);
        vbo.bindColorBuffer(GL15.GL_STATIC_DRAW);

        riverVbos.put(chunk, vbo);
    }

    private void initHexVbos() {
        // Initialize the mouse-over VBO
        mouseOverVbo = new VertexBufferObject.Builder()
            .setNumVertices(WorldScreenHelper.NUM_VERTICES)
            .setColorMode(VertexBufferObject.ColorMode.RGBA)
            .setDrawFunction(() -> GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0,
                                                     WorldScreenHelper.NUM_VERTICES))
            .build();

        // Add each vertex in the tile, with corresponding color
        for (Point vertex : WorldScreenHelper.TILE_VERTICES) {
            mouseOverVbo.addVertex(vertex);
            mouseOverVbo.addColor(Colors.MOUSE_OVER);
        }

        mouseOverVbo.bindVertexBuffer(GL15.GL_STATIC_DRAW);
        mouseOverVbo.bindColorBuffer(GL15.GL_STATIC_DRAW);

        hexVertexHandle = mouseOverVbo.getVertexHandle(); // Save this for future hexagons
    }

    @Override
    public void draw(Point mousePos) {
        // Mouse pos is null when being rendered in the background (behind a menu)
        if (mousePos == null) {
            lastMouseDragPos = null; // No longer dragging
        } else {
            updateMouseOver(mousePos); // Update state based on mouse position
        }

        GL11.glPushMatrix();
        GL11.glTranslated(worldCenter.x(), worldCenter.y(), 0.0);
        GL11.glScaled(worldScale, worldScale, 1.0);

        // Draw each chunk
        for (Map.Entry<Chunk, VertexBufferObject> entry : tileVbos.entrySet()) {
            drawChunk(entry.getKey(), entry.getValue());
        }

        if (mouseOverTile != null) {
            drawMouseOverHighlight();
        }

        GL11.glPopMatrix();

        // If debug mode is enabled, draw debug info
        if (getDebug()) {
            drawDebugInfo();
        }

        super.draw(mousePos); // Draw GUI elements
    }

    /**
     * Updates state based on the current mouse position. The mouse-over tile is updated, the
     * world is shifted if the user is dragging the mouse, etc.
     *
     * @param mousePos the current position of the mouse
     */
    private void updateMouseOver(Point mousePos) {
        // If the mouse is being dragged, shift the world center based on it.
        // Otherwise, draw info for the tile that the mouse is currently over.
        if (lastMouseDragPos != null) {
            mouseOverTile = null; // No highlight while dragging
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            worldCenter = worldCenter.plus(diff);
            lastMouseDragPos = mousePos; // Update the mouse pos
        } else {
            // Update which tile the mouse is over
            mouseOverTile = calcTileUnderMouse(mousePos);
            if (mouseOverTile != null) {
                // Draw the overlay then set text for the info box
                mouseOverTileInfo.setText(mouseOverTile.info(getDebug()));
            }
        }
        mouseOverTileInfo.setVisible(mouseOverTile != null);
        mouseOverTileInfo.updatePosition(mousePos);
    }

    private Tile calcTileUnderMouse(Point mousePos) {
        // Shift and scale the mouse pos to align with the world
        final Point fixedMousePos = mousePos.minus(worldCenter).scale(1.0 / worldScale);

        // Get the tile that the mouse is over and return it
        final HexPoint mouseOverPos = WorldScreenHelper.pixelToTile(fixedMousePos);
        return worldHandler.getWorld().getTiles().getByPoint(mouseOverPos);
    }

    private void drawMouseOverHighlight() {
        final Point tilePos = WorldScreenHelper.tileToPixel(mouseOverTile.pos());
        GL11.glPushMatrix();
        GL11.glTranslated(tilePos.x(), tilePos.y(), 0.0);

        mouseOverVbo.draw();

        GL11.glPopMatrix();
    }

    private void drawChunk(Chunk chunk, VertexBufferObject vbo) {
        // Translate to this chunk's pixel position
        GL11.glPushMatrix();
        GL11.glTranslated(chunk.getScreenPos().x(), chunk.getScreenPos().y(), 0.0);

        vbo.draw();
        riverVbos.get(chunk).draw();

        GL11.glPopMatrix();
    }

    private void drawDebugInfo() {
        final String debugString = String.format(WorldScreenHelper.FPS_FORMAT, getFps());
        renderer().drawString(Font.DEBUG, debugString, 10, 10); // Draw FPS
    }

    private void updateAllTileColors() {
        final long startTime = System.currentTimeMillis();
        for (Map.Entry<Chunk, VertexBufferObject> entry : tileVbos.entrySet()) {
            updateChunkColors(entry.getKey(), entry.getValue());
        }
        final long endTime = System.currentTimeMillis();
        logger.log(Level.FINER, String.format("Color update took %d ms", endTime - startTime));
    }

    /**
     * Updates the color of each tile in the given chunk. The chunk's VBO is passed so it doesn't
     * have to be looked up. This is generally going to be called from iteration over all chunks,
     * so the VBO should be readily available and lookup can be avoided.
     *
     * @param chunk the chunk to update
     * @param vbo   the vbo for this chunk
     */
    private void updateChunkColors(Chunk chunk, VertexBufferObject vbo) {
        // Bind the handle for the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo.getColorHandle());
        long offset = 0;
        for (Tile tile : chunk.getTiles()) {
            final Color color = getTileColor(tile);
            for (int i = 0; i < WorldScreenHelper.NUM_VERTICES; i++) {
                offset += vbo.setVertexColor(offset, color);
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
        final Color baseColor = tile.getColor(tileColorMode);
        final Color overlayColor = getTileOverlayColor(tile);

        // If there is an overlay color, mix the two colors
        if (overlayColor != null) {
            return Funcs.overlayColors(overlayColor, baseColor);
        }
        return baseColor;
    }

    /**
     * Gets the current overlay color for the given tile. This color is based on the current
     * value of {@link #tileOverlay}. This should be added to the tile's base color to generate
     * its displayed color.
     *
     * @param tile the tile whose color we are retrieving
     * @return the overlay color, or {@code null} if this tile has no active overlay
     */
    private Color getTileOverlayColor(Tile tile) {
        switch (tileOverlay) {
            case CONTINENT:
                final Continent continent =
                    worldHandler.getWorld().getTilesToContinents().get(tile);
                if (continent != null) {
                    // Draw an overlay in the continent's color
                    return continent.getOverlayColor();
                }
                break;
            case CHUNK:
                return tile.getChunk().getOverlayColor();
        }
        return null;
    }

    private void setTileColorMode(TileColorMode colorMode) {
        tileColorMode = colorMode;
        updateAllTileColors();
    }

    private void setTileOverlay(WorldScreenHelper.TileOverlay overlay) {
        // If this overlay is already enabled, disable it, otherwise switch to it
        if (overlay == tileOverlay) {
            tileOverlay = WorldScreenHelper.TileOverlay.NONE;
        } else {
            tileOverlay = overlay;
        }

        updateAllTileColors();
    }

    private void zoom(double step) {
        final double oldScale = worldScale;
        worldScale = WorldScreenHelper.VALID_WORLD_SCALES.coerce(worldScale + step);
        // Adjust the world center so that the tile at the center of the screen stays there
        worldCenter = worldCenter
            .minus(WorldScreenHelper.SCREEN_CENTER)
            .scale(worldScale / oldScale)
            .plus(WorldScreenHelper.SCREEN_CENTER);
    }

    private void regenerateWorld() {
        worldHandler.generate();
        updateAllTileColors();
    }

    private void stepWorld() {
        // Step then refresh the world
        worldHandler.step();
        updateAllTileColors();
    }

    @Override
    public void onKey(KeyEvent event) {
        if (event.action == ButtonAction.RELEASE) {
            switch (event.command) {
                case GAME_MENU:
                    setNextScreen(new PauseScreen(this)); // Open the pause menu
                    break;
                case WORLD_REGEN:
                    regenerateWorld();
                    break;
                case WORLD_NEXT_STEP:
                    stepWorld();
                    break;
                case WORLD_TILE_COLOR_ELEVATION:
                    setTileColorMode(TileColorMode.ELEVATION);
                    break;
                case WORLD_TILE_COLOR_HUMIDITY:
                    setTileColorMode(TileColorMode.HUMIDITY);
                    break;
                case WORLD_TILE_COLOR_WATER_LEVEL:
                    setTileColorMode(TileColorMode.WATER_LEVEL);
                    break;
                case WORLD_TILE_COLOR_WATER_TRAVERSED:
                    setTileColorMode(TileColorMode.WATER_TRAVERSED);
                    break;
                case WORLD_TILE_COLOR_BIOME:
                    setTileColorMode(TileColorMode.BIOME);
                    break;
                case WORLD_TILE_COLOR_COMPOSITE:
                    setTileColorMode(TileColorMode.COMPOSITE);
                    break;
                case WORLD_TILE_OVERLAY_CONTINENTS:
                    setTileOverlay(WorldScreenHelper.TileOverlay.CONTINENT);
                    break;
                case WORLD_TILE_OVERLAY_CHUNKS:
                    setTileOverlay(WorldScreenHelper.TileOverlay.CHUNK);
                    break;
            }
        }
        super.onKey(event);
    }

    @Override
    public void onClick(MouseButtonEvent event) {
        switch (event.button) {
            case GLFW.GLFW_MOUSE_BUTTON_1:
                if (event.action == ButtonAction.PRESS) {
                    lastMouseDragPos = event.mousePos;
                    mouseDownTime = System.currentTimeMillis();
                } else if (event.action == ButtonAction.RELEASE) {
                    // If the elapsed time between mouse down and up is below a threshold, call it a click
                    if (System.currentTimeMillis() - mouseDownTime
                        <= WorldScreenHelper.MAX_CLICK_TIME) {
                        super.onClick(event);
                    }
                    lastMouseDragPos = null; // Wipe this out
                }
                break;
        }
    }

    @Override
    public void onScroll(ScrollEvent event) {
        if (event.yOffset < 0) {
            // Zoom out
            zoom(-WorldScreenHelper.ZOOM_STEP);
        } else if (event.yOffset > 0) {
            // Zoom in
            zoom(WorldScreenHelper.ZOOM_STEP);
        }
    }
}
