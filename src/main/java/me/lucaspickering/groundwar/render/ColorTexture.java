package me.lucaspickering.groundwar.render;

import java.awt.Color;

import me.lucaspickering.groundwar.TerrainGen;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;

public class ColorTexture {

    // Tile overlays
    public static final ColorTexture mouseOver = new ColorTexture(Constants.TILE_BG_NAME,
                                                                  Colors.MOUSE_OVER);

    private final Texture texture;
    private final Color color;

    private ColorTexture(String texName, Color color) {
        final Renderer renderer = TerrainGen.instance().renderer();
        if (!renderer.hasTexture(texName)) {
            renderer.loadTexture(texName);
        }
        texture = renderer.getTexture(texName);
        this.color = color;
    }

    public void draw(int x, int y, int width, int height) {
        texture.draw(x, y, width, height, color);
    }
}
