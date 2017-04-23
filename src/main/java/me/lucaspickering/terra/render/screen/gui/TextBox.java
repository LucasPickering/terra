package me.lucaspickering.terra.render.screen.gui;

import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Objects;

import me.lucaspickering.terra.render.Font;
import me.lucaspickering.terra.render.HorizAlignment;
import me.lucaspickering.terra.render.VertAlignment;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.utils.Pair;
import me.lucaspickering.utils.Point;

public class TextBox extends GuiElement {

    private static final int BORDER_PADDING_X = 8;
    private static final int BORDER_PADDING_Y = 1;
    private static final Font FONT = Font.TILE;

    private String text;
    private Color textColor = Color.WHITE;

    public TextBox() {
        this("", Point.ZERO);
    }

    public TextBox(String text, Point pos) {
        super(pos);
        setText(text);
    }

    public TextBox(String text, Point pos, HorizAlignment horizAlign, VertAlignment vertAlign) {
        super(pos, 0, 0, horizAlign, vertAlign);
        setText(text);
    }

    public String getText() {
        return text;
    }

    public TextBox setText(String text) {
        Objects.requireNonNull(text);
        this.text = text;
        final Pair<Integer, Integer> stringSize = renderer().getStringSize(text, FONT);
        setWidth(stringSize.first() + BORDER_PADDING_X * 2);
        setHeight(stringSize.second() + BORDER_PADDING_Y * 2);
        return this;
    }

    @Override
    public void draw(Point mousePos) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        renderer().drawRect(0, 0, getWidth(), getHeight(), Colors.TILE_INFO_BG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        renderer().drawString(FONT, text, BORDER_PADDING_X, BORDER_PADDING_Y + 10, textColor);
    }
}
