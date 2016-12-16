package me.lucaspickering.groundwar.render.screen.gui;

import java.util.Objects;

import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.render.screen.ScreenElement;
import me.lucaspickering.groundwar.util.Point;

public abstract class GuiElement implements ScreenElement {

    private Point pos;
    private int width;
    private int height;
    private HorizAlignment horizAlign;
    private VertAlignment vertAlign;
    private boolean visible = true;
    private boolean enabled = true;

    /**
     * Constructs a new {@code GuiElement} with the given position.
     *
     * @param pos the position of the element
     */
    protected GuiElement(Point pos) {
        this.pos = pos;
        this.horizAlign = HorizAlignment.LEFT;
        this.vertAlign = VertAlignment.TOP;
    }

    /**
     * Constructs a new {@code GuiElement} with the given coordinates and size.
     *
     * @param pos    the position of the element
     * @param width  the width of the element (non-negative)
     * @param height the height of the element (non-negative)
     * @throws IllegalArgumentException if width or height is non-positive
     */
    protected GuiElement(Point pos, int width, int height) {
        this(pos, width, height, HorizAlignment.LEFT, VertAlignment.TOP);
    }

    /**
     * Constructs a new {@code GuiElement} with the given coordinates and alignments.
     *
     * @param pos        the position of the element
     * @param horizAlign the horizontal alignment of the element (non-null)
     * @param vertAlign  the vertical alignment of the element (non-null)
     * @throws NullPointerException if {@code horizAlign == null} or {@code vertAlign == null}
     */
    protected GuiElement(Point pos, HorizAlignment horizAlign, VertAlignment vertAlign) {
        Objects.requireNonNull(horizAlign);
        Objects.requireNonNull(vertAlign);

        this.horizAlign = horizAlign;
        this.vertAlign = vertAlign;
        setPos(pos);
    }

    /**
     * Constructs a new {@code GuiElement} with the given coordinates, size, and alignments.
     *
     * @param pos        the position of the element
     * @param width      the width of the element (non-negative)
     * @param height     the height of the element (non-negative)
     * @param horizAlign the horizontal alignment of the element (non-null)
     * @param vertAlign  the vertical alignment of the element (non-null)
     * @throws IllegalArgumentException if width or height is non-positive
     * @throws NullPointerException     if {@code horizAlign == null} or {@code vertAlign == null}
     */
    protected GuiElement(Point pos, int width, int height,
                         HorizAlignment horizAlign, VertAlignment vertAlign) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width or height out of bounds");
        }
        Objects.requireNonNull(horizAlign);
        Objects.requireNonNull(vertAlign);

        this.width = width;
        this.height = height;
        this.horizAlign = horizAlign;
        this.vertAlign = vertAlign;
        setPos(pos);
    }

    public final Point getPos() {
        return pos;
    }

    public final void setPos(Point pos) {
        this.pos = pos.adjustForAlignment(horizAlign, vertAlign, width, height);
    }

    public final int getX() {
        return pos.x();
    }

    public final int getY() {
        return pos.y();
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final boolean isEnabled() {
        return visible && enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean contains(Point p) {
        return pos.x() <= p.x() && p.x() <= pos.x() + width
               && pos.y() <= p.y() && p.y() <= pos.y() + height;
    }
}
