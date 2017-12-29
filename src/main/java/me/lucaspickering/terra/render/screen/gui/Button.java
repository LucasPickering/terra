package me.lucaspickering.terra.render.screen.gui;

import me.lucaspickering.terra.render.Font;
import me.lucaspickering.terra.render.HorizAlignment;
import me.lucaspickering.terra.render.VertAlignment;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.util.Constants;
import me.lucaspickering.utils.Point2;

public class Button extends GuiElement {

    private static final int WIDTH = 650;
    private static final int HEIGHT = 150;

    private String text;

    public Button(String text, Point2 pos) {
        this(text, pos, HorizAlignment.CENTER, VertAlignment.CENTER);
    }

    public Button(String text, Point2 pos, HorizAlignment horizAlign, VertAlignment vertAlign) {
        super(pos, WIDTH, HEIGHT, horizAlign, vertAlign);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(Point2 mousePos) {
        final boolean mouseOver = contains(mousePos);
        renderer().drawTexture(Constants.TEX_BUTTON, 0, 0, getWidth(), getHeight());
        renderer().drawString(Font.UI, text, getWidth() / 2, getHeight() / 2,
                              mouseOver ? Colors.BUTTON_TEXT_HIGHLIGHT : Colors.BUTTON_TEXT_NORMAL,
                              HorizAlignment.CENTER, VertAlignment.CENTER);
    }
}
