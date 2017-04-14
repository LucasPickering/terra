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

import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.event.ScrollEvent;
import me.lucaspickering.terraingen.render.screen.gui.MouseTextBox;
import me.lucaspickering.terraingen.util.Colors;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.Continent;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.TileColorMode;
import me.lucaspickering.terraingen.world.WorldHandler;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.utils.Point;

public class WorldScreen extends Screen {

    private enum TileOverlay {
        NONE, CONTINENT
    }

    // Maximum time a click can be held down to be considered a click and not a drag
    private static final int MAX_CLICK_TIME = 250;

    // Change of tile size in pixels with each zoom level
    private static final double ZOOM_STEP = 1.0;

    private static final int NUM_VERTICES = WorldHandler.TILE_VERTICES.length;
    private static final int VERTEX_SIZE = 2;
    private static final int COLOR_SIZE = 3;

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
        }};

    private final WorldHandler worldHandler;
    private final MouseTextBox mouseOverTileInfo;

    private TileColorMode tileColorMode = TileColorMode.COMPOSITE;
    private TileOverlay tileOverlay = TileOverlay.NONE;

    // The last position of the mouse while dragging. Null if not dragging.
    private Point lastMouseDragPos;

    // The time at which the user pressed the mouse button down
    private long mouseDownTime;

    private TileSet onScreenTiles;

    private int vboVertexHandle;
    private int vboColorHandle;

    public WorldScreen(WorldHandler worldHandler) {
        Objects.requireNonNull(worldHandler);
        this.worldHandler = worldHandler;
        mouseOverTileInfo = new MouseTextBox();
        mouseOverTileInfo.setVisible(false); // Hide this for now
        addGuiElement(mouseOverTileInfo);
        updateOnScreenTiles();
        initVbo();
    }

    private void initVbo() {
        DoubleBuffer vertexBuffer = BufferUtils.createDoubleBuffer(VERTEX_SIZE * NUM_VERTICES);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(COLOR_SIZE * NUM_VERTICES);
        for (Point vertex : WorldHandler.TILE_VERTICES) {
            vertexBuffer.put(new double[]{vertex.x(), vertex.y()});
            colorBuffer.put(new float[]{1f, 0f, 0f});
        }
        vertexBuffer.flip();
        colorBuffer.flip();

        vboVertexHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        vboColorHandle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void draw(Point mousePos) {
        // Mouse pos is null when being rendered in the background
        if (mousePos == null) {
            lastMouseDragPos = null; // No longer dragging
        }

        updateScreenCenter(mousePos);

        GL11.glPushMatrix();
        final double scale = worldHandler.getWorldScale();
        GL11.glScaled(scale, scale, 0.0);

        // Draw each tile
        for (Tile tile : onScreenTiles) {
            drawTile(tile);
        }

        // Draw the overlay for the tile that the mouse is over, then draw the text box with info
        // for that tile.
        final Tile mouseOverTile = getMouseOverTile(mousePos);
        mouseOverTileInfo.setVisible(mouseOverTile != null);
        if (mouseOverTile != null) {
            // Draw the overlay then set text for the info box
            drawMouseOverlay(mouseOverTile);
            mouseOverTileInfo.setText(mouseOverTile.info());
        }

        GL11.glPopMatrix();

        super.draw(mousePos); // Draw GUI elements
    }

    private void updateScreenCenter(Point mousePos) {
        // If the mouse is being dragged, shift the world center based on it
        if (lastMouseDragPos != null) {
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            worldHandler.setWorldCenter(worldHandler.getWorldCenter().plus(diff));
            lastMouseDragPos = mousePos; // Update the mouse pos
            updateOnScreenTiles(); // Refresh the set of tiles that are on screen
        }
    }

    private void updateOnScreenTiles() {
        // This is a silly way. Gets the furthest tile from the center that is still on screen,
        // then renders all tiles in that range. Could definitely be more efficient.
        final TilePoint topLeftTilePos = worldHandler.pixelToTile(Point.ZERO);
        final TilePoint centerTilePos = worldHandler.pixelToTile(center);
        final int screenRadius = topLeftTilePos.distanceTo(centerTilePos);

        onScreenTiles = worldHandler.getWorld().getTiles().getTilesInRange(centerTilePos,
                                                                           screenRadius);
    }

    /**
     * Draws the given tile.
     *
     * @param tile the tile to draw
     */
    private void drawTile(Tile tile) {
        // Shift to the tile's center
        final Point tileCenter = worldHandler.getTileCenter(tile);
        GL11.glPushMatrix();
        GL11.glTranslated(tileCenter.x(), tileCenter.y(), 0.0);

        // Draw the background of the tile
        drawHex(tile.getColor(tileColorMode));
        drawTileOverlays(tile);

        GL11.glPopMatrix();
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
        Funcs.setGlColor(color);

        // Set up the vertex buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle);
        GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_DOUBLE, 0, 0L);

        // Set up the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle);
//        GL11.glColorPointer(COLOR_SIZE, GL11.GL_FLOAT, 0, 0L);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glDrawArrays(GL11.GL_POLYGON, 0, NUM_VERTICES);

//        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private Tile getMouseOverTile(Point mousePos) {
        if (mousePos == null) {
            return null;
        }

        // Get the tile that the mouse is over and return it
        final TilePoint mouseOverPos = worldHandler.pixelToTile(mousePos);
        return worldHandler.getWorld().getTiles().getByPoint(mouseOverPos);
    }

    private void zoom(double step) {
        worldHandler.adjustWorldScale(step);
        updateOnScreenTiles(); // Refresh the set of on-screen tiles
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
