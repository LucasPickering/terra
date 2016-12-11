package me.lucaspickering.groundwar.render.screen.gui;

import org.lwjgl.opengl.GL11;

import java.awt.Color;

import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;

public class TextDisplay extends GuiElement {

    private static final int TEXT_OFFSET_X = 8;

    private String text;
    private Color textColor = Colors.WHITE;

    public TextDisplay(String text, Point pos, int width, int height) {
        super(pos, width, height);
        this.text = text;
    }

    public TextDisplay(String text, Point pos, int width, int height,
                       HorizAlignment horizAlign, VertAlignment vertAlign) {
        super(pos, width, height, horizAlign, vertAlign);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(Point mousePos) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        renderer().drawRect(0, 0, getWidth(), getHeight(), Colors.TILE_INFO_BG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        renderer().drawString(Constants.FONT_SIZE_TILE, text, TEXT_OFFSET_X, 0, textColor);
    }
}
