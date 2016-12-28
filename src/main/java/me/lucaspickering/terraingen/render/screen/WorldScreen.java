package me.lucaspickering.terraingen.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Map;

import me.lucaspickering.terraingen.render.ColorTexture;
import me.lucaspickering.terraingen.render.HorizAlignment;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.VertAlignment;
import me.lucaspickering.terraingen.render.event.KeyEvent;
import me.lucaspickering.terraingen.render.screen.gui.TextDisplay;
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

    // Location of the tile info box relative to the cursor
    private static final int TILE_INFO_OFFSET_X = 20;
    private static final int TILE_INFO_OFFSET_Y = -10;

    // The range of elevation differences that the outline width varies across
    private static final InclusiveRange ELEV_DIFF_RANGE = new InclusiveRange(0, 20);
    private static final float DEFAULT_OUTLINE_WIDTH = 1.5f;
    private static final float MIN_OUTLINE_WIDTH = 1f;
    private static final float MAX_OUTLINE_WIDTH = 4f;

    private final World world;
    private final TextDisplay mouseOverTileInfo;

    public WorldScreen(World world) {
        this.world = world;
        addGuiElement(mouseOverTileInfo = new TextDisplay());
        mouseOverTileInfo.setVisible(false);
    }

    @Override
    public void draw(Point mousePos) {
        final Map<TilePoint, Tile> tileMap = world.getTiles();
        final Collection<Tile> tiles = tileMap.values();

        // Draw each tile. For each one, check if it is the
        final TilePoint mouseOverPos = WorldHelper.pixelToTile(mousePos);
        tiles.forEach(tile -> drawTile(tile, tile.pos().equals(mouseOverPos)));

        // Update mouseOverTileInfo for the tile that the mouse is over. This HAS to be done
        // after all the tiles are drawn, otherwise it would be underneath some of them.
        final Tile mouseOverTile = tileMap.get(mouseOverPos);
        if (mouseOverTile != null) {
            mouseOverTileInfo.setVisible(true);
            mouseOverTileInfo.setText(mouseOverTile.info());

            int x = TILE_INFO_OFFSET_X;
            int y = TILE_INFO_OFFSET_Y;
            HorizAlignment horizAlign = HorizAlignment.LEFT;
            VertAlignment vertAlign = VertAlignment.BOTTOM;

            // If the box extends outside the screen on the right, move it left of the cursor
            if (mousePos.x() + x + mouseOverTileInfo.getWidth() > Renderer.RES_WIDTH) {
                x *= -1;
                horizAlign = HorizAlignment.RIGHT;
            }

            // If it extends off the top of the screen, move it below the cursor
            if (mousePos.y() + y - mouseOverTileInfo.getHeight() < 0) {
                y *= -1;
                vertAlign = VertAlignment.TOP;
            }

            mouseOverTileInfo.setPos(mousePos.plus(x, y));
            mouseOverTileInfo.setHorizAlign(horizAlign);
            mouseOverTileInfo.setVertAlign(vertAlign);
        }

        super.draw(mousePos); // Draw GUI elements
        mouseOverTileInfo.setVisible(false); // Hide the tile info, to be updated on the next frame
    }

    /**
     * Draws the given tile.
     *
     * @param tile      the tile to draw
     * @param mouseOver is the mouse currently over this tile?
     */
    private void drawTile(Tile tile, boolean mouseOver) {
        GL11.glPushMatrix();

        // Translate to the top-left corner of the tile
        final Point tileTopLeft = tile.topLeft();
        GL11.glTranslatef(tileTopLeft.x(), tileTopLeft.y(), 0f);

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

        // Start drawing textures
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Translate to the top-left of the tile
        drawTileOverlays(tile, mouseOver); // Draw the tile overlays on top of everything else

        // Stop drawing textures
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    /**
     * Draw the appropriate overlays for the given tile.
     *
     * @param tile      the tile to draw
     * @param mouseOver is the mouse currently over this tile?
     */
    private void drawTileOverlays(Tile tile, boolean mouseOver) {
        final int width = Tile.WIDTH;
        final int height = Tile.HEIGHT;

        // Draw mouse-over overlays
        if (mouseOver) { // If the mouse is over this tile...
            ColorTexture.mouseOver.draw(0, 0, width, height); // Draw the mouse-over overlay
        }
    }

    @Override
    public void onKey(KeyEvent event) {
        switch (event.key) {
            case GLFW.GLFW_KEY_SPACE:
                // todo pause generation here
                break;
            case GLFW.GLFW_KEY_ESCAPE:
                setNextScreen(null); // Close the game
        }
    }
}
