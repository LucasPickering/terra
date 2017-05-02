package me.lucaspickering.terra.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

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
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.TileOverlayMode;
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
    private TileOverlayMode tileOverlayMode = TileOverlayMode.NONE;

    private Point lastMouseDragPos; // The last position of the mouse while dragging, or null
    private Tile mouseOverTile; // The tile that the mouse is currently over
    private long mouseDownTime; // The time at which the user pressed the mouse button down

    private final Logger logger;

    // CHUNK VBO FIELDS
    private final HexPointMap<Chunk, ChunkVbo> chunkVbos = new HexPointMap<>();

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
        // Init a VBO container for each chunk
        for (Chunk chunk : worldHandler.getWorld().getChunks()) {
            chunkVbos.put(chunk, new ChunkVbo(this, chunk));
        }

        initHexVbos(); // Init single-tile VBOs, such as mouse-over highlight
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
        for (ChunkVbo vbo : chunkVbos.values()) {
            vbo.draw();
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

    private void drawDebugInfo() {
        final String debugString = String.format(WorldScreenHelper.FPS_FORMAT, getFps());
        renderer().drawString(Font.DEBUG, debugString, 10, 10); // Draw FPS
    }

    private void updateAllTileColors() {
        final long startTime = System.currentTimeMillis();
        for (ChunkVbo vbo : chunkVbos.values()) {
            vbo.updateColors();
        }
        final long endTime = System.currentTimeMillis();
        logger.log(Level.FINER, String.format("Color update took %d ms", endTime - startTime));
    }

    public TileColorMode getTileColorMode() {
        return tileColorMode;
    }

    private void setTileColorMode(TileColorMode colorMode) {
        tileColorMode = colorMode;
        updateAllTileColors();
    }

    public TileOverlayMode getTileOverlayMode() {
        return tileOverlayMode;
    }

    private void setTileOverlayMode(TileOverlayMode overlayMode) {
        // If this overlay is already enabled, disable it, otherwise switch to it
        if (overlayMode == tileOverlayMode) {
            tileOverlayMode = TileOverlayMode.NONE;
        } else {
            tileOverlayMode = overlayMode;
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
                    setTileOverlayMode(TileOverlayMode.CONTINENT);
                    break;
                case WORLD_TILE_OVERLAY_CHUNKS:
                    setTileOverlayMode(TileOverlayMode.CHUNK);
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
