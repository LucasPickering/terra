package me.lucaspickering.terraingen.render.screen.gui;

import me.lucaspickering.terraingen.render.HorizAlignment;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.render.VertAlignment;
import me.lucaspickering.utils.Point;

public class MouseTextBox extends TextBox {

    // Location of this box relative to the cursor
    private static final int OFFSET_X = 20;
    private static final int OFFSET_Y = -10;

    public MouseTextBox() {
        this("");
    }

    public MouseTextBox(String text) {
        super(text, Point.ZERO, HorizAlignment.LEFT, VertAlignment.BOTTOM);
    }

    @Override
    public void draw(Point mousePos) {
        // These will get changed if the box extends off the screen, to make it fit
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

        // Set some parameters
        setPos(mousePos.plus(x, y));
        setHorizAlign(horizAlign);
        setVertAlign(vertAlign);

        super.draw(mousePos); // Draw the box
    }
}
