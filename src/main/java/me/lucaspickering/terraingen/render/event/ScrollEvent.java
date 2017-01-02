package me.lucaspickering.terraingen.render.event;

import me.lucaspickering.terraingen.util.Point;

public class ScrollEvent extends Event {

    public final double xOffset, yOffset;
    public final Point mousePos;

    /**
     * Constructs a new {@link ScrollEvent}.
     *
     * @param window   the window
     * @param xOffset  x scroll dimension
     * @param yOffset  y scroll dimension (negative is down, positive is up)
     * @param mousePos the position of the mouse
     */
    public ScrollEvent(long window, double xOffset, double yOffset, Point mousePos) {
        super(window);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.mousePos = mousePos;
    }
}
