package me.lucaspickering.terraingen.render.screen.gui;

import java.util.Objects;

import me.lucaspickering.terraingen.render.HorizAlignment;
import me.lucaspickering.terraingen.render.VertAlignment;
import me.lucaspickering.terraingen.render.screen.ScreenElement;
import me.lucaspickering.terraingen.util.Point;

public abstract class GuiElement implements ScreenElement {

    // This is only retained so adjustedPos can be updated after changing alignments
    private Point pos;
    private Point adjustedPos; // The position after being adjusted for alignments
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

    public Point getPos() {
        return adjustedPos;
    }

    public void setPos(Point pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
        updateAdjustedPos();
    }

    public final int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public final HorizAlignment getHorizAlign() {
        return horizAlign;
    }

    public void setHorizAlign(HorizAlignment horizAlign) {
        Objects.requireNonNull(horizAlign);
        this.horizAlign = horizAlign;
        updateAdjustedPos();
    }

    public final VertAlignment getVertAlign() {
        return vertAlign;
    }

    public void setVertAlign(VertAlignment vertAlign) {
        Objects.requireNonNull(vertAlign);
        this.vertAlign = vertAlign;
        updateAdjustedPos();
    }

    public final boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final boolean isEnabled() {
        return visible && enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean contains(Point p) {
        return adjustedPos.x() <= p.x() && p.x() <= adjustedPos.x() + width
               && adjustedPos.y() <= p.y() && p.y() <= adjustedPos.y() + height;
    }

    private void updateAdjustedPos() {
        adjustedPos = pos.adjustForAlignment(horizAlign, vertAlign, width, height);
    }
}
