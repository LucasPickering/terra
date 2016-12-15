package me.lucaspickering.groundwar.render.screen.gui;

import me.lucaspickering.groundwar.render.Font;
import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;

public class Button extends GuiElement {

    private static final int WIDTH = 650;
    private static final int HEIGHT = 150;

    private String text;

    public Button(String text, Point pos) {
        super(pos, WIDTH, HEIGHT);
        this.text = text;
    }

    public Button(String text, Point pos, HorizAlignment horizAlign, VertAlignment vertAlign) {
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
    public void draw(Point mousePos) {
        final boolean mouseOver = contains(mousePos);
        renderer().drawTexture(Constants.BUTTON_NAME, 0, 0, getWidth(), getHeight());
        renderer().drawString(Font.UI, text, getWidth() / 2, getHeight() / 2,
                              mouseOver ? Colors.BUTTON_TEXT_HIGHLIGHT : Colors.BUTTON_TEXT_NORMAL,
                              HorizAlignment.CENTER, VertAlignment.CENTER);
    }
}
