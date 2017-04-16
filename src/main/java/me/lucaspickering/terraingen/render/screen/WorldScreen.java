package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.Texture;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.event.ScrollEvent;
import me.lucaspickering.terraingen.render.screen.gui.MouseTextBox;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.terraingen.util.Constants;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.TileColorMode;
import me.lucaspickering.terraingen.world.WorldHandler;
import me.lucaspickering.terraingen.world.util.Chunk;
import me.lucaspickering.terraingen.world.util.HexPoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.Pair;
import me.lucaspickering.utils.Point;
import me.lucaspickering.utils.range.DoubleRange;
import me.lucaspickering.utils.range.Range;

public class WorldScreen extends Screen {

    private enum TileOverlay {
        NONE, CONTINENT, CHUNK
    }

    private static final Range<Double> VALID_WORLD_SCALES = new DoubleRange(0.5, 10.0);

    // Maximum time a click can be held down to be considered a click and not a drag
    private static final int MAX_CLICK_TIME = 250;

    // Change of tile size in pixels with each zoom level
    private static final double ZOOM_STEP = 1.0;

    // Each side of the tile is rendered by forming a triangle between it and the center, so
    // there's three vertices for each side of the tile.
    private static final int NUM_VERTICES = Direction.values().length * 3;
    private static final int VERTEX_SIZE = 2;
    private static final int COLOR_SIZE = 4;

    // Bind a key to each tile color mode
    private static final Map<Integer, TileColorMode> keyToTileColorMode =
        new HashMap<Integer, TileColorMode>() {{
            put(GLFW.GLFW_KEY_F1, TileColorMode.ELEVATION);
            put(GLFW.GLFW_KEY_F2, TileColorMode.HUMIDITY);
            put(GLFW.GLFW_KEY_F3, TileColorMode.BIOME);
            put(GLFW.GLFW_KEY_F4, TileColorMode.COMPOSITE);
        }};

    // Assign a key to each tile overlay
    private static final Map<Integer, TileOverlay> keyToTileOverlay =
        new HashMap<Integer, TileOverlay>() {{
            put(GLFW.GLFW_KEY_F5, TileOverlay.CONTINENT);
            put(GLFW.GLFW_KEY_F6, TileOverlay.CHUNK);
        }};

    private final WorldHandler worldHandler;
    private final Texture tileTexture;
    private final MouseTextBox mouseOverTileInfo;
    private Point worldCenter; // The pixel location of the center of the world
    private double worldScale = 1.0;

    private TileColorMode tileColorMode = TileColorMode.COMPOSITE;
    private TileOverlay tileOverlay = TileOverlay.NONE;

    // The last position of the mouse while dragging. Null if not dragging.
    private Point lastMouseDragPos;

    // The time at which the user pressed the mouse button down
    private long mouseDownTime;

    private final Map<Chunk, Pair<Integer, Integer>> chunkVboMap = new TreeMap<>();

    public WorldScreen(WorldHandler worldHandler) {
        Objects.requireNonNull(worldHandler);
        this.worldHandler = worldHandler;
        tileTexture = renderer().loadTexture(Constants.TEX_HEXAGON);
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
        mouseOverTileInfo = new MouseTextBox();
        mouseOverTileInfo.setVisible(false); // Hide this for now
        addGuiElement(mouseOverTileInfo);
        initVbos();
    }

    private void initVbos() {
        for (Chunk chunk : worldHandler.getWorld().getChunks().values()) {
            initVboForChunk(chunk);
        }
    }

    private void initVboForChunk(Chunk chunk) {
        final TileSet tiles = chunk.getTiles();

        // Allocate and populate vertex and color buffers
        final DoubleBuffer vertexBuffer =
            BufferUtils.createDoubleBuffer(VERTEX_SIZE * NUM_VERTICES * tiles.size());
        final FloatBuffer colorBuffer =
            BufferUtils.createFloatBuffer(COLOR_SIZE * NUM_VERTICES * tiles.size());
        final float[] colorArray = new float[COLOR_SIZE]; // Color components will be stored here
        for (Tile tile : tiles) {
            final Point tileCenter = worldHandler.getTileCenter(tile);

            // Store the RGBA components of this tile's color in an array
            final Color color = tile.getColor(tileColorMode);
            color.getRGBComponents(colorArray);
            for (int i = 0; i < WorldHandler.TILE_VERTICES.length; i++) {
                final Point vertex1 = WorldHandler.TILE_VERTICES[i];
                final Point vertex2 = WorldHandler.TILE_VERTICES[(i + 1) %
                                                                 WorldHandler.TILE_VERTICES.length];
                vertexBuffer.put(tileCenter.x() + vertex1.x());
                vertexBuffer.put(tileCenter.y() + vertex1.y());
                vertexBuffer.put(tileCenter.x() + vertex2.x());
                vertexBuffer.put(tileCenter.y() + vertex2.y());
                vertexBuffer.put(tileCenter.x());
                vertexBuffer.put(tileCenter.y());

                colorBuffer.put(colorArray);
                colorBuffer.put(colorArray);
                colorBuffer.put(colorArray);
            }
        }
        vertexBuffer.flip();
        colorBuffer.flip();

        final int vboVertexHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        final int vboColorHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        chunkVboMap.put(chunk, new Pair<>(vboVertexHandle, vboColorHandle));
    }

    @Override
    public void draw(Point mousePos) {
        // Mouse pos is null when being rendered in the background
        if (mousePos == null) {
            lastMouseDragPos = null; // No longer dragging
        }

        updateScreenCenter(mousePos);

        GL11.glPushMatrix();
        GL11.glTranslated(worldCenter.x(), worldCenter.y(), 0.0);
        GL11.glScaled(worldScale, worldScale, 1.0);

        processMouseOver(mousePos);

        for (Pair<Integer, Integer> vboHandles : chunkVboMap.values()) {
            drawChunk(vboHandles.first(), vboHandles.second());
        }

        GL11.glPopMatrix();

        super.draw(mousePos); // Draw GUI elements
    }

    private void processMouseOver(Point mousePos) {
        // Draw the overlay for the tile that the mouse is over, then draw the text box with info
        // for that tile.
        final Tile mouseOverTile = getMouseOverTile(mousePos);
        mouseOverTileInfo.setVisible(mouseOverTile != null);
        if (mouseOverTile != null) {
            // Draw the overlay then set text for the info box
            drawMouseOverlay(mouseOverTile);
            mouseOverTileInfo.setText(mouseOverTile.info());
        }
    }

    private void updateScreenCenter(Point mousePos) {
        // If the mouse is being dragged, shift the world center based on it
        if (lastMouseDragPos != null) {
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            worldCenter = worldCenter.plus(diff);
            lastMouseDragPos = mousePos; // Update the mouse pos
        }
    }

    private void drawChunk(int vertexHandle, int colorHandle) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexHandle);
        GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_DOUBLE, 0, 0L);

        // Set up the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);
        GL11.glColorPointer(COLOR_SIZE, GL11.GL_FLOAT, 0, 0L);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, NUM_VERTICES * Chunk.CHUNK_SIZE);

        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    /**
     * Draw the appropriate overlays for the given tile.
     *
     * @param tile the tile to draw
     */
    private void drawTileOverlays(Tile tile) {
        // If debug mode is enabled, display a color unique(ish) to this tile's continent
        switch (tileOverlay) {
            case CONTINENT:
                final Continent continent =
                    worldHandler.getWorld().getTilesToContinents().get(tile);
                if (continent != null) {
                    // Draw an overlay in the continent's color
                    drawHex(continent.getOverlayColor());
                }
                break;
            case CHUNK:
                drawHex(tile.getChunk().getOverlayColor());
                break;
        }
    }

    private void drawMouseOverlay(Tile tile) {
        final Point tileCenter = worldHandler.getTileCenter(tile);
        GL11.glPushMatrix();
        GL11.glTranslated(tileCenter.x(), tileCenter.y(), 0.0);

        drawHex(Colors.MOUSE_OVER);

        GL11.glPopMatrix();
    }

    /**
     * Draws a hexagon centered at the current GL position, with the given color.
     *
     * @param color the color for the hexagon
     */
    private void drawHex(Color color) {

    }

    private Tile getMouseOverTile(Point mousePos) {
        if (mousePos == null) {
            return null;
        }

        // Get the tile that the mouse is over and return it
        final HexPoint mouseOverPos = worldHandler.pixelToTile(mousePos.minus(worldCenter));
        return worldHandler.getWorld().getTiles().getByPoint(mouseOverPos);
    }

    private void zoom(double step) {
        worldScale = VALID_WORLD_SCALES.coerce(worldScale + step);
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
                    // Re-generate the world
                    worldHandler.generateParallel();
                    break;
            }

            // Check if the key is assigned to a tile color mode
            final TileColorMode keyTileColorMode = keyToTileColorMode.get(key);
            if (keyTileColorMode != null) {
                tileColorMode = keyTileColorMode;
            }

            // Check if the key is assigned to a tile overlay
            final TileOverlay keyTileOverlay = keyToTileOverlay.get(key);
            if (keyTileOverlay != null) {
                // If this overlay is already selected, turn it off, otherwise select it
                if (keyTileOverlay == tileOverlay) {
                    tileOverlay = TileOverlay.NONE;
                } else {
                    tileOverlay = keyTileOverlay;
                }
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
                    if (System.currentTimeMillis() - mouseDownTime <= MAX_CLICK_TIME) {
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
            zoom(-ZOOM_STEP);
        } else if (event.yOffset > 0) {
            // Zoom in
            zoom(ZOOM_STEP);
        }
    }
}
