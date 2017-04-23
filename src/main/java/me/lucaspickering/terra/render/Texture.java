package me.lucaspickering.terra.render;

import org.lwjgl.opengl.GL11;

import java.awt.Color;

import me.lucaspickering.terra.util.Funcs;

public class Texture {

    private final int textureID;

    public Texture(int textureID) {
        this.textureID = textureID;
    }

    /**
     * Draws this texture at the given location and size, with the given color.
     *  @param x      the x-location of the top-left of the texture
     * @param y      the y-location of the top-left of the texture
     * @param width  the width of the texture
     * @param height the height of the texture
     * @param color  the color of the texture
     */
    public void draw(double x, double y, double width, double height, Color color) {
        Funcs.setGlColor(color); // Set the color
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind the texture

        // Draw a rectangle
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2d(0, 0);
            GL11.glVertex2d(x, y);

            GL11.glTexCoord2d(1, 0);
            GL11.glVertex2d(x + width, y);

            GL11.glTexCoord2d(1, 1);
            GL11.glVertex2d(x + width, y + height);

            GL11.glTexCoord2d(0, 1);
            GL11.glVertex2d(x, y + height);
        }
        GL11.glEnd();
    }

    public void delete() {
        GL11.glDeleteTextures(textureID);
    }
}
