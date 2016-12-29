package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Map;

import me.lucaspickering.terraingen.render.ColorTexture;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.event.MouseButtonEvent;
import me.lucaspickering.terraingen.render.screen.gui.MouseTextBox;
import me.lucaspickering.terraingen.util.Constants;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.WorldHelper;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldScreen extends MainScreen {

    // Maximum time a click can be held down to be considered a click and not a drag
    private static final int MAX_CLICK_TIME = 250;

    // The range of elevation differences that the outline width varies across
    private static final InclusiveRange ELEV_DIFF_RANGE = new InclusiveRange(0, 20);
    private static final float DEFAULT_OUTLINE_WIDTH = 1.5f;
    private static final float MIN_OUTLINE_WIDTH = 1f;
    private static final float MAX_OUTLINE_WIDTH = 4f;

    private final World world;
    private final MouseTextBox mouseOverTileInfo;

    // The last position of the mouse while dragging. Null if not dragging.
    private Point lastMouseDragPos;

    // The time at which the user pressed the mouse button down
    private long mouseDownTime;

    public WorldScreen(World world) {
        this.world = world;
        addGuiElement(mouseOverTileInfo = new MouseTextBox());
        mouseOverTileInfo.setVisible(false);
    }

    @Override
    public void draw(Point mousePos) {
        // If the mouse is being dragged, shift the world center based on it
        Point worldCenter = world.getWorldCenter();
        if (lastMouseDragPos != null) {
            // Shift the world
            final Point diff = mousePos.minus(lastMouseDragPos);
            worldCenter = worldCenter.plus(diff);
            world.setWorldCenter(worldCenter);
            lastMouseDragPos = mousePos; // Update the mouse pos
        }

        final Map<TilePoint, Tile> tileMap = world.getTiles();
        final Collection<Tile> tiles = tileMap.values();

        // Draw each tile. For each one, check if it is the
        final Point shiftedMousePos = mousePos.minus(worldCenter);
        final TilePoint mouseOverPos = WorldHelper.pixelToTile(shiftedMousePos);

        // Draw each tile
        {
            GL11.glTranslatef(worldCenter.x(), worldCenter.y(), 0f);
            // Draw the tiles themselves
            tiles.forEach(this::drawTile);

            // Draw the overlays
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            tiles.forEach(tile -> drawTileOverlays(tile, tile.pos().equals(mouseOverPos)));
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glTranslatef(-worldCenter.x(), -worldCenter.y(), 0f);
        }

        // Update mouseOverTileInfo for the tile that the mouse is over. This HAS to be done
        // after all the tiles are drawn, otherwise it would be underneath some of them.
        final Tile mouseOverTile = tileMap.get(mouseOverPos);
        if (mouseOverTile != null) {
            mouseOverTileInfo.setVisible(true);
            mouseOverTileInfo.setText(mouseOverTile.info());
        }

        super.draw(mousePos); // Draw GUI elements
        mouseOverTileInfo.setVisible(false); // Hide the tile info, to be updated on the next frame
    }

    /**
     * Draws the given tile.
     *
     * @param tile      the tile to draw
     */
    private void drawTile(Tile tile) {
        // Translate to the top-left corner of the tile
        GL11.glTranslatef(tile.topLeft().x(), tile.topLeft().y(), 0f);

        // Start drawing textures
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Draw the tile background
        renderer().drawTexture(Constants.TILE_BG_NAME, 0, 0, Tile.WIDTH, Tile.HEIGHT,
                               tile.backgroundColor());

        // Stop drawing textures
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        // Draw the outline by drawing each side as an individual line.
        for (int i = 0; i < Tile.NUM_SIDES; i++) {
            // Get the two vertices that the line will be between
            final Point vertex1 = Tile.VERTICES[i];
            final Point vertex2 = Tile.VERTICES[(i + 1) % Tile.NUM_SIDES];
            final Direction dir = Direction.values()[i];

            // The line width is based on the elevation between this tile and the adjacent one
            final float lineWidth;
            final Tile adjTile = tile.adjacents().get(dir); // Get the adjacent tile
            if (adjTile != null) {
                // If it exists, calculate line width
                final int elevDiff = Math.abs(tile.elevation() - adjTile.elevation());
                lineWidth = ELEV_DIFF_RANGE.normalize(elevDiff,
                                                      MIN_OUTLINE_WIDTH, MAX_OUTLINE_WIDTH);
            } else {
                // If there is no adjacent tile in this direction, use the default line width
                lineWidth = DEFAULT_OUTLINE_WIDTH;
            }
            GL11.glLineWidth(lineWidth);

            Funcs.setGlColor(tile.outlineColor());
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(vertex1.x(), vertex1.y());
            GL11.glVertex2i(vertex2.x(), vertex2.y());
            GL11.glEnd();
        }

        // Translate back
        GL11.glTranslatef(-tile.topLeft().x(), -tile.topLeft().y(), 0f);
    }

    /**
     * Draw the appropriate overlays for the given tile.
     *
     * @param tile      the tile to draw
     * @param mouseOver is the mouse currently over this tile?
     */
    private void drawTileOverlays(Tile tile, boolean mouseOver) {
        // Translate to this tile
        GL11.glTranslatef(tile.topLeft().x(), tile.topLeft().y(), 0f);

        // If the mouse is over this tile, draw the mouse-over overlay
        if (mouseOver) {
            ColorTexture.mouseOver.draw(0, 0, Tile.WIDTH, Tile.HEIGHT);
        }

        // Translate back
        GL11.glTranslatef(-tile.topLeft().x(), -tile.topLeft().y(), 0f);
    }

    @Override
    public void onKey(KeyEvent event) {
        if (event.action == GLFW.GLFW_RELEASE) {
            switch (event.key) {
                case GLFW.GLFW_KEY_SPACE:
                    // todo pause generation here
                    break;
                case GLFW.GLFW_KEY_ESCAPE:
                    setNextScreen(null); // Close the game
            }
        }
    }

    @Override
    public void onClick(MouseButtonEvent event) {
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_1) {
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
        }
    }
}
