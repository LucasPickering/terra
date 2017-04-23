package me.lucaspickering.terra.render.screen;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.render.event.KeyEvent;
import me.lucaspickering.terra.render.event.MouseButtonEvent;
import me.lucaspickering.terra.render.event.ScrollEvent;
import me.lucaspickering.terra.render.screen.gui.MouseTextBox;
import me.lucaspickering.terra.util.Colors;
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

    // We frequently have to use float arrays for color purposes, so just allocate one
    private final float[] colorArray = new float[WorldScreenHelper.COLOR_SIZE];

    // CHUNK VBO FIELDS
    private int chunkVertexHandle;
    private final HexPointMap<Chunk, Integer> chunkColorHandles = new HexPointMap<>();
    private final int[] startingIndices = new int[Chunk.TOTAL_TILES];
    private final int[] sizes = new int[Chunk.TOTAL_TILES];

    private int hexVertexHandle;
    private int mouseOverColorHandle;

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
        initChunkVertexVbo();
        for (Chunk chunk : worldHandler.getWorld().getChunks()) {
            initChunkVbo(chunk);
        }
        initHexVbos(); // Init single-tile VBOs, such as mouse-over highlight
    }

    private void initChunkVertexVbo() {
        // Allocate the vertex buffer
        final DoubleBuffer vertexBuffer = BufferUtils.createDoubleBuffer(
            WorldScreenHelper.VERTEX_SIZE * WorldScreenHelper.NUM_VERTICES * Chunk.TOTAL_TILES);

        // Populate the buffer with each vertex for each tile
        for (int x = 0; x < Chunk.SIDE_LENGTH; x++) {
            for (int y = 0; y < Chunk.SIDE_LENGTH; y++) {
                final Point tileCenter = WorldScreenHelper.tileToPixel(new HexPoint(x, y));
                for (Point vertex : WorldScreenHelper.TILE_VERTICES) {
                    // Shift this vertex by the tile's center, and add it to the buffer
                    vertexBuffer.put(tileCenter.x() + vertex.x());
                    vertexBuffer.put(tileCenter.y() + vertex.y());
                }
            }
        }
        vertexBuffer.flip();

        // Bind the buffer to the GL
        chunkVertexHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, chunkVertexHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        // Populate the array that tells GL which vertex to start at for each polygon, and
        // another that tells it how big (in vertices) each polygon is.
        for (int i = 0; i < Chunk.TOTAL_TILES; i++) {
            startingIndices[i] = i * WorldScreenHelper.NUM_VERTICES;
            sizes[i] = WorldScreenHelper.NUM_VERTICES;
        }
    }

    private void initChunkVbo(Chunk chunk) {
        chunk.setScreenPos(WorldScreenHelper.chunkToPixel(chunk.getPos()));

        // Allocate the color buffer
        final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(
            WorldScreenHelper.COLOR_SIZE * WorldScreenHelper.NUM_VERTICES * Chunk.TOTAL_TILES);

        // Bind the color buffer to the GL
        final int colorHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_DYNAMIC_DRAW);
        GL30.glBindVertexArray(0);

        // Put the handle in the map, then populate the buffer with the correct colors
        chunkColorHandles.put(chunk, colorHandle);
        updateChunkColors(chunk, colorHandle);
    }

    private void initHexVbos() {
        // Allocate the buffers
        final DoubleBuffer vertexBuffer = BufferUtils.createDoubleBuffer(
            WorldScreenHelper.VERTEX_SIZE * WorldScreenHelper.NUM_VERTICES);
        final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(
            4 * WorldScreenHelper.NUM_VERTICES);

        // Populate the buffer with each vertex
        float[] f = Colors.MOUSE_OVER.getColorComponents(null);
        for (Point vertex : WorldScreenHelper.TILE_VERTICES) {
            vertexBuffer.put(vertex.x());
            vertexBuffer.put(vertex.y());
            colorBuffer.put(f);
        }
        vertexBuffer.flip();
        colorBuffer.flip();

        // Bind the buffer to the GL
        hexVertexHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, chunkVertexHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        // Allocate the color buffer

        // Bind the color buffer for the mouse highlight overlay to the GL
        mouseOverColorHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mouseOverColorHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void draw(Point mousePos) {
        // Mouse pos is null when being rendered in the background
        if (mousePos == null) {
            lastMouseDragPos = null; // No longer dragging
        }

        processMouse(mousePos); // Update state based on mouse position

        GL11.glPushMatrix();
        GL11.glTranslated(worldCenter.x(), worldCenter.y(), 0.0);
        GL11.glScaled(worldScale, worldScale, 1.0);

        // Draw each chunk
        for (Map.Entry<Chunk, Integer> entry : chunkColorHandles.entrySet()) {
            drawChunk(entry.getKey(), entry.getValue());
        }

        drawMouseOverHighlight();

        GL11.glPopMatrix();

        super.draw(mousePos); // Draw GUI elements
    }

    private void processMouse(Point mousePos) {
        // If the mouse is being dragged, shift the world center based on it.
        // Otherwise, draw info for the tile that the mouse is currently over.
        if (lastMouseDragPos != null) {
            mouseOverTile = null; // While dragging,
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            worldCenter = worldCenter.plus(diff);
            lastMouseDragPos = mousePos; // Update the mouse pos
        } else {
            // Update which tile the mouse is over, update state if that value changed since last
            // frame, then update the text for the tile.
            final Tile newMouseOverTile = calcTileUnderMouse(mousePos);
            if (newMouseOverTile != mouseOverTile) {
                changeMouseOverTile(newMouseOverTile);
            }
            if (mouseOverTile != null) {
                // Draw the overlay then set text for the info box
                mouseOverTileInfo.setText(mouseOverTile.info());
            }
        }
    }

    private void drawMouseOverHighlight() {
        final Point tilePos = WorldScreenHelper.tileToPixel(mouseOverTile.pos());
        GL11.glPushMatrix();
        GL11.glTranslated(tilePos.x(), tilePos.y(), 0.0);

        // Set up the vertex buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, hexVertexHandle);
        GL11.glVertexPointer(WorldScreenHelper.VERTEX_SIZE, GL11.GL_DOUBLE, 0, 0L);

        // Set up the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mouseOverColorHandle);
        GL11.glColorPointer(WorldScreenHelper.COLOR_SIZE, GL11.GL_FLOAT, 0, 0L);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        // Draw all tiles in the chunk
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 6);

        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glPopMatrix();
    }

    private void changeMouseOverTile(Tile newMouseOverTile) {
        // Set the old tile's color back to normal
        if (mouseOverTile != null) {
//            setTileColor(mouseOverTile, getTileColor(mouseOverTile));
        }

        // If the mouse is over a new tile now, change its color
        if (newMouseOverTile != null) {
            final Color tileColor = Funcs.overlayColors(Colors.MOUSE_OVER,
                                                        getTileColor(newMouseOverTile));
//            setTileColor(newMouseOverTile, tileColor);
        }

        mouseOverTile = newMouseOverTile; // Make the switch
        mouseOverTileInfo.setVisible(mouseOverTile != null);
    }

    private Tile calcTileUnderMouse(Point mousePos) {
        // Shift and scale the mouse pos to align with the world
        final Point fixedMousePos = mousePos.minus(worldCenter).scale(1.0 / worldScale);

        // Get the tile that the mouse is over and return it
        final HexPoint mouseOverPos = WorldScreenHelper.pixelToTile(fixedMousePos);
        return worldHandler.getWorld().getTiles().getByPoint(mouseOverPos);
    }

    private void drawChunk(Chunk chunk, int colorHandle) {
        // Translate to this chunk's pixel position
        final Point chunkPos = chunk.getScreenPos();
        GL11.glPushMatrix();
        GL11.glTranslated(chunkPos.x(), chunkPos.y(), 0.0);

        // Set up the vertex buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, chunkVertexHandle);
        GL11.glVertexPointer(WorldScreenHelper.VERTEX_SIZE, GL11.GL_DOUBLE, 0, 0L);

        // Set up the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);
        GL11.glColorPointer(WorldScreenHelper.COLOR_SIZE, GL11.GL_FLOAT, 0, 0L);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        // Draw all tiles in the chunk
        GL14.glMultiDrawArrays(GL11.GL_TRIANGLE_FAN, startingIndices, sizes);

        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glPopMatrix();
    }

    private void updateAllTileColors() {
        final long startTime = System.currentTimeMillis();
        for (Map.Entry<Chunk, Integer> entry : chunkColorHandles.entrySet()) {
            updateChunkColors(entry.getKey(), entry.getValue());
        }
        final long endTime = System.currentTimeMillis();
        logger.log(Level.FINEST, String.format("Color update took %d ms", endTime - startTime));
    }

    /**
     * Updates the color of each tile in the given chunk. The chunk's VBO handles are passed so
     * that they don't have to be looked up. This is generally going to be called from iteration
     * over all chunks, so the handles should be readily available and lookup can be avoided.
     *
     * @param chunk       the chunk to update
     * @param colorHandle the handle for the given chunk's color VBO
     */
    private void updateChunkColors(Chunk chunk, int colorHandle) {
        // Bind the handle for the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);
        long offset = 0;
        for (Tile tile : chunk.getTiles()) {
            updateColor(getTileColor(tile), offset);
            offset += WorldScreenHelper.COLOR_SIZE_BYTES * WorldScreenHelper.NUM_VERTICES;
        }
    }

    /**
     * Sets the color of the given tile to be the given value. This finds the VBO that the given
     * tile belongs to, and modifies its color buffer to contain the given color.
     *
     * @param tile  the tile whose color will be changed
     * @param color the new color for the tile
     */
    private void setTileColor(Tile tile, Color color) {
        // Find the handle for the appropriate color VBO and bind it
        final HexPoint chunkPos = Chunk.getChunkPosForTile(tile.pos());
        final int colorHandle = chunkColorHandles.getByPoint(chunkPos);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);

        final HexPoint relPos = Chunk.getRelativeTilePos(tile.pos());
        final long offset = (relPos.x() * Chunk.SIDE_LENGTH + relPos.y()) *
                            WorldScreenHelper.NUM_VERTICES * WorldScreenHelper.COLOR_SIZE_BYTES;
        updateColor(color, offset);
    }

    /**
     * Updates the color at the given offset in the buffer that is currently bound. The color buffer
     * MUST be bound before calling this(using {@link GL15#glBindBuffer(int, int)}, or it will not
     * work.
     *
     * @param color  the new color
     * @param offset the byte offset for the tile whose color you want to change
     */
    private void updateColor(Color color, long offset) {
        color.getColorComponents(colorArray);

        // Change the color for each vertex of this tile
        for (int i = 0; i < WorldScreenHelper.NUM_VERTICES; i++) {
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, colorArray);
            offset += WorldScreenHelper.COLOR_SIZE_BYTES; // Move up to the next color
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
        if (event.action == GLFW.GLFW_RELEASE) {
            final int key = event.key;
            switch (key) {
                case GLFW.GLFW_KEY_ESCAPE:
                    setNextScreen(new PauseScreen(this)); // Open the pause menu
                    break;
                case GLFW.GLFW_KEY_R:
                    regenerateWorld();
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                    stepWorld();
                    break;
            }

            // Check if the key is assigned to a tile color mode
            final TileColorMode
                keyTileColorMode =
                WorldScreenHelper.KEY_TO_TILE_COLOR_MODE.get(key);
            if (keyTileColorMode != null) {
                setTileColorMode(keyTileColorMode);
            }

            // Check if the key is assigned to a tile overlay
            final WorldScreenHelper.TileOverlay keyTileOverlay =
                WorldScreenHelper.KEY_TO_TILE_OVERLAY.get(key);
            if (keyTileOverlay != null) {
                setTileOverlay(keyTileOverlay);
            }
        }
        super.onKey(event);
    }

    @Override
    public void onClick(MouseButtonEvent event) {
        switch (event.button) {
            case GLFW.GLFW_MOUSE_BUTTON_1:
                if (event.action == GLFW.GLFW_PRESS) {
                    lastMouseDragPos = event.mousePos;
                    mouseDownTime = System.currentTimeMillis();
                } else if (event.action == GLFW.GLFW_RELEASE) {
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
