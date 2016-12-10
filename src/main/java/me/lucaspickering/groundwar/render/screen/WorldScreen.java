package me.lucaspickering.groundwar.render.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

import me.lucaspickering.groundwar.render.ColorTexture;
import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.render.event.KeyEvent;
import me.lucaspickering.groundwar.render.screen.gui.TextDisplay;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;
import me.lucaspickering.groundwar.world.World;
import me.lucaspickering.groundwar.world.tile.Tile;

public class WorldScreen extends MainScreen {

    private static final Point TILE_INFO_POS = new Point(20, -10);
    private static final int TILE_INFO_WIDTH = 370;
    private static final int TILE_INFO_HEIGHT = 200;

    private final World world;
    private final TextDisplay mouseOverTileInfo;

    public WorldScreen(World world) {
        this.world = world;
        addGuiElement(mouseOverTileInfo = new TextDisplay(null, new Point(), 0, 0,
                                                          HorizAlignment.LEFT,
                                                          VertAlignment.BOTTOM));
        mouseOverTileInfo.setVisible(false);
    }

    @Override
    public void draw(Point mousePos) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        final Collection<Tile> tiles = world.getTiles().values();

        tiles.forEach(tile -> drawTile(tile, mousePos)); // Draw each tile

        // Update mouseOverTileInfo for the unit that the mouse is over
        for (Tile tile : tiles) {
            if (tile.contains(mousePos)) {
                mouseOverTileInfo.setText(null); // todo tile info
                mouseOverTileInfo.setPos(mousePos.plus(TILE_INFO_POS));
                mouseOverTileInfo.setWidth(TILE_INFO_WIDTH);
                mouseOverTileInfo.setHeight(TILE_INFO_HEIGHT);
                mouseOverTileInfo.setTextColor(0); // tile color
                mouseOverTileInfo.setVisible(true);
                break; // We don't need to check the rest of the tiles
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        super.draw(mousePos); // Draw GUI elements
        mouseOverTileInfo.setVisible(false); // Hide the unit info, to be updated on the next frame
    }

    /**
     * Draws the given tile.
     *
     * @param tile     the tile to draw
     * @param mousePos the position of the mouse
     */
    private void drawTile(Tile tile, Point mousePos) {
        GL11.glPushMatrix();
        GL11.glTranslatef(tile.getScreenPos().getX(), tile.getScreenPos().getY(), 0f);

        final int width = Constants.TILE_WIDTH;
        final int height = Constants.TILE_HEIGHT;

        // Draw the regular background
        renderer().drawTexture(Constants.TILE_BG_NAME, 0, 0, width, height,
                               tile.getBackgroundColor());

        // Draw the regular foreground
        renderer().drawTexture(Constants.TILE_OUTLINE_NAME, 0, 0, width, height,
                               tile.getOutlineColor());

        drawTileOverlays(tile, mousePos); // Draw the tile overlays on top of everything else

        GL11.glPopMatrix();
    }

    /**
     * Draw the appropriate overlays for the given tile.
     *
     * @param tile     the tile to draw
     * @param mousePos the position of the mouse
     */
    private void drawTileOverlays(Tile tile, Point mousePos) {
        final int width = Constants.TILE_WIDTH;
        final int height = Constants.TILE_HEIGHT;

        // Draw mouse-over overlays
        if (tile.contains(mousePos)) { // If the mouse is over this tile...
            ColorTexture.mouseOver.draw(0, 0, width, height); // Draw the mouse-over overlay
        }
    }

    @Override
    public void onKey(KeyEvent event) {
        switch (event.key) {
            case GLFW.GLFW_KEY_SPACE:
                // todo pause generation here
                break;
        }
    }
}
