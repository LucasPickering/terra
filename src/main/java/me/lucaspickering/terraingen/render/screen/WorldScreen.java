package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.lucaspickering.terraingen.render.ColorTexture;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.event.ScrollEvent;
import me.lucaspickering.terraingen.render.screen.gui.MouseTextBox;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.Tiles;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldScreen extends Screen {

    // Maximum time a click can be held down to be considered a click and not a drag
    private static final int MAX_CLICK_TIME = 250;

    private static final float OUTLINE_WIDTH = 1.5f;

    private final World world;
    private final MouseTextBox mouseOverTileInfo;

    // The last position of the mouse while dragging. Null if not dragging.
    private Point lastMouseDragPos;

    // The time at which the user pressed the mouse button down
    private long mouseDownTime;

    public WorldScreen(World world) {
        Objects.requireNonNull(world);
        this.world = world;
        mouseOverTileInfo = new MouseTextBox();
        mouseOverTileInfo.setVisible(false); // Hide this for now
        addGuiElement(mouseOverTileInfo);
    }

    @Override
    public void draw(Point mousePos) {
        // Mouse pos is null when being rendered in the background
        if (mousePos == null) {
            lastMouseDragPos = null; // No longer dragging
        }

        // If the mouse is being dragged, shift the world center based on it
        if (lastMouseDragPos != null) {
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            world.setWorldCenter(world.getWorldCenter().plus(diff));
            lastMouseDragPos = mousePos; // Update the mouse pos
        }

        final Tiles tiles = world.getTiles();

        // Get all the tiles that are on-screen (those are the ones that will be drawn)
        final List<Tile> onScreenTiles = tiles.stream()
            .filter(this::containsTile)
            .collect(Collectors.toList());

        // If there is a mouse position, check which tile it's over
        final TilePoint mouseOverPos;
        if (mousePos != null) {
            mouseOverPos = WorldHelper.pixelToTile(world, mousePos);
        } else {
            mouseOverPos = null;
        }

        // Draw each tile
        {
            // Draw the tiles themselves
            onScreenTiles.forEach(this::drawTile);

            // Draw the overlays
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            onScreenTiles.forEach(tile -> drawTileOverlays(tile, tile.pos().equals(mouseOverPos)));
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }

        // Update mouseOverTileInfo for the tile that the mouse is over. This HAS to be done
        // after all the tiles are drawn, otherwise it would be underneath some of them.
        final Tile mouseOverTile = tiles.getByPoint(mouseOverPos);
        if (mouseOverTile != null) {
            // Set the text and show the element
            mouseOverTileInfo.setText(mouseOverTile.info()).setVisible(true);
        }

        super.draw(mousePos); // Draw GUI elements
        mouseOverTileInfo.setVisible(false); // Hide the tile info, to be updated on the next frame
    }

    private boolean containsTile(Tile tile) {
        // If any of the 4 corners of the tile are on-screen, the tile is on-screen
        return contains(world.getTileTopLeft(tile))
               || contains(world.getTileTopRight(tile))
               || contains(world.getTileBottomRight(tile))
               || contains(world.getTileBottomLeft(tile));
    }

    /**
     * Draws the given tile.
     *
     * @param tile the tile to draw
     */
    private void drawTile(Tile tile) {
        // Shift to the tile and draw the background
        GL11.glPushMatrix();
        final Point tileCenter = world.getTileCenter(tile);
        GL11.glTranslatef(tileCenter.x(), tileCenter.y(), 0f);
        drawTileBackground(tile);
        // Could draw tile outlines here
        GL11.glPopMatrix();
    }

    private void drawTileBackground(Tile tile) {
        // Set the color then draw a hexagon
        Funcs.setGlColor(tile.backgroundColor());
        GL11.glBegin(GL11.GL_POLYGON);
        for (Point vertex : world.getTileVertices()) {
            GL11.glVertex2i(vertex.x(), vertex.y());
        }
        GL11.glEnd();
    }

    private void drawTileOutline(Tile tile) {
        for (int i = 0; i < Tile.NUM_SIDES; i++) {
            // Get the two vertices that the line will be between
            final Point vertex1 = world.getTileVertices()[i];
            final Point vertex2 = world.getTileVertices()[(i + 1) % Tile.NUM_SIDES];

            // The line width is based on the elevation between this tile and the adjacent one
            GL11.glLineWidth(OUTLINE_WIDTH);
            Funcs.setGlColor(tile.outlineColor());
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(vertex1.x(), vertex1.y());
            GL11.glVertex2i(vertex2.x(), vertex2.y());
            GL11.glEnd();
        }
    }

    /**
     * Draw the appropriate overlays for the given tile.
     *
     * @param tile      the tile to draw
     * @param mouseOver is the mouse currently over this tile?
     */
    private void drawTileOverlays(Tile tile, boolean mouseOver) {
        // Translate to this tile
        GL11.glPushMatrix();
        final Point tileTopLeft = world.getTileTopLeft(tile);
        GL11.glTranslatef(tileTopLeft.x(), tileTopLeft.y(), 0f);

        // If the mouse is over this tile, draw the mouse-over overlay
        if (mouseOver) {
            ColorTexture.mouseOver.draw(0, 0, world.getTileWidth(), world.getTileHeight());
        }

        GL11.glPopMatrix();
    }

    @Override
    public void onKey(KeyEvent event) {
        if (event.action == GLFW.GLFW_RELEASE) {
            switch (event.key) {
                case GLFW.GLFW_KEY_ESCAPE:
                    setNextScreen(new PauseScreen(this)); // Open the pause menu
                    break;
            }
        }
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
            world.setTileRadius(world.getTileRadius() - 5);
        } else if (event.yOffset > 0) {
            // Zoom in
            world.setTileRadius(world.getTileRadius() + 5);
        }
    }
}
