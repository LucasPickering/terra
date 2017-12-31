package me.lucaspickering.terra.input;

import me.lucaspickering.utils.Point2;

public class ScrollEvent extends Event {

    public final double xOffset, yOffset;
    public final Point2 mousePos;

    /**
     * Constructs a new {@link ScrollEvent}.
     *
     * @param xOffset  x scroll dimension
     * @param yOffset  y scroll dimension (negative is down, positive is up)
     * @param mousePos the position of the mouse
     */
    public ScrollEvent(double xOffset, double yOffset, Point2 mousePos) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.mousePos = mousePos;
    }
}
