package me.lucaspickering.terra.render.screen.gui;

import java.util.function.Consumer;

import me.lucaspickering.terra.input.MouseButtonEvent;
import me.lucaspickering.terra.render.Font;
import me.lucaspickering.terra.render.HorizAlignment;
import me.lucaspickering.terra.render.VertAlignment;
import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.util.Constants;
import me.lucaspickering.utils.Point2;

public class Button extends GuiElement {

    private static final int DEFAULT_WIDTH = 650;
    private static final int DEFAULT_HEIGHT = 150;

    public static class Builder {

        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private Point2 pos;
        private HorizAlignment horizAlign = HorizAlignment.CENTER;
        private VertAlignment vertAlign = VertAlignment.CENTER;
        private String text;
        private Consumer<MouseButtonEvent> clickHandler;

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder pos(Point2 pos) {
            this.pos = pos;
            return this;
        }

        public Builder horizAlign(HorizAlignment horizAlign) {
            this.horizAlign = horizAlign;
            return this;
        }

        public Builder vertAlign(VertAlignment vertAlign) {
            this.vertAlign = vertAlign;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder clickHandler(Consumer<MouseButtonEvent> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        public Button build() {
            if (pos == null) {
                throw new NullPointerException("Position cannot be null");
            }
            if (text == null) {
                throw new NullPointerException("Text cannot be null");
            }
            return new Button(text, width, height, pos, horizAlign, vertAlign, clickHandler);
        }
    }

    private final String text;
    private final Consumer<MouseButtonEvent> clickHandler;

    private Button(String text, int width, int height, Point2 pos,
                   HorizAlignment horizAlign, VertAlignment vertAlign,
                   Consumer<MouseButtonEvent> clickHandler) {
        super(pos, width, height, horizAlign, vertAlign);
        this.clickHandler = clickHandler;
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

    @Override
    public void onClick(MouseButtonEvent event) {
        // If a handler is registered for this element, call it
        if (this.clickHandler != null) {
            this.clickHandler.accept(event);
        }
    }
}
