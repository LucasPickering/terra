package me.lucaspickering.terra.render.screen;

import me.lucaspickering.utils.Point2;

/**
 * A {@code ScreenElement} is anything that be drawn into the game window. A {@code ScreenElement}
 * can hold children elements, and is responsible for calling {@link #draw} for those children. How
 * each {@code ScreenElement} contains and handles its children is up to each element.
 */
public interface ScreenElement {

    /**
     * Draws this screen onto the window.
     */
    void draw();

    /**
     * Checks if this element contains the given point. Bounds checking is inclusive on all sides.
     *
     * @param p the point to be checked
     * @return true if p falls inside this element, false otherwise
     */
    boolean contains(Point2 p);
}
