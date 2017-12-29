package me.lucaspickering.terra.render.screen.gui;

import me.lucaspickering.terra.render.HorizAlignment;
import me.lucaspickering.terra.render.Renderer;
import me.lucaspickering.terra.render.VertAlignment;
import me.lucaspickering.utils.Point2;

public class MouseTextBox extends TextBox {

    // Location of this box relative to the cursor
    private static final int OFFSET_X = 20;
    private static final int OFFSET_Y = -10;

    public MouseTextBox() {
        this("");
    }

    public MouseTextBox(String text) {
        super(text, Point2.ZERO, HorizAlignment.LEFT, VertAlignment.BOTTOM);
    }

    public void updatePosition(Point2 mousePos) {
        int x = OFFSET_X;
        int y = OFFSET_Y;
        HorizAlignment horizAlign = HorizAlignment.LEFT;
        VertAlignment vertAlign = VertAlignment.BOTTOM;

        // If the box extends outside the screen on the right, move it left of the cursor
        if (mousePos.x() + x + getWidth() > Renderer.RES_WIDTH) {
            x *= -1;
            horizAlign = HorizAlignment.RIGHT;
        }

        // If it extends off the top of the screen, move it below the cursor
        if (mousePos.y() + y - getHeight() < 0) {
            y *= -1;
            vertAlign = VertAlignment.TOP;
        }
        setPos(mousePos.plus(x, y));
        setHorizAlign(horizAlign);
        setVertAlign(vertAlign);
    }
}
