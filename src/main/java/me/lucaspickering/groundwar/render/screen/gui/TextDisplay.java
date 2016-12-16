package me.lucaspickering.groundwar.render.screen.gui;

import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Objects;

import me.lucaspickering.groundwar.render.Font;
import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Point;

public class TextDisplay extends GuiElement {

    private static final int BORDER_PADDING_X = 8;
    private static final int BORDER_PADDING_Y = 1;
    private static final Font FONT = Font.TILE;

    private String text;
    private Color textColor = Color.WHITE;

    public TextDisplay(String text, Point pos) {
        super(pos);
        setText(text);
    }

    public TextDisplay(String text, Point pos, HorizAlignment horizAlign, VertAlignment vertAlign) {
        super(pos, horizAlign, vertAlign);
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        Objects.requireNonNull(text);
        this.text = text;
        setWidth(renderer().getStringWidth(text, FONT) + BORDER_PADDING_X * 2);
        setHeight(renderer().getStringHeight(text, FONT) + BORDER_PADDING_Y * 2);
    }

    @Override
    public void draw(Point mousePos) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        renderer().drawRect(0, 0, getWidth(), getHeight(), Colors.TILE_INFO_BG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        renderer().drawString(FONT, text, BORDER_PADDING_X, 0, textColor);
    }
}
