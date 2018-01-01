package me.lucaspickering.terra.render.screen.gui;

import java.util.Objects;

import me.lucaspickering.terra.input.MouseButtonEvent;
import me.lucaspickering.terra.render.HorizAlignment;
import me.lucaspickering.terra.render.VertAlignment;
import me.lucaspickering.terra.render.screen.ScreenElement;
import me.lucaspickering.utils.Point2;

public abstract class GuiElement implements ScreenElement {

    // This is only retained so adjustedPos can be updated after changing alignments
    private Point2 pos;
    private Point2 adjustedPos; // The position after being adjusted for alignments
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
    protected GuiElement(Point2 pos) {
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
    protected GuiElement(Point2 pos, int width, int height) {
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
    protected GuiElement(Point2 pos, int width, int height,
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

    public Point2 getPos() {
        return adjustedPos;
    }

    public GuiElement setPos(Point2 pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
        updateAdjustedPos();
        return this;
    }

    public final int getWidth() {
        return width;
    }

    public GuiElement setWidth(int width) {
        this.width = width;
        return this;
    }

    public final int getHeight() {
        return height;
    }

    public GuiElement setHeight(int height) {
        this.height = height;
        return this;
    }

    public final HorizAlignment getHorizAlign() {
        return horizAlign;
    }

    public GuiElement setHorizAlign(HorizAlignment horizAlign) {
        Objects.requireNonNull(horizAlign);
        this.horizAlign = horizAlign;
        updateAdjustedPos();
        return this;
    }

    public final VertAlignment getVertAlign() {
        return vertAlign;
    }

    public GuiElement setVertAlign(VertAlignment vertAlign) {
        Objects.requireNonNull(vertAlign);
        this.vertAlign = vertAlign;
        updateAdjustedPos();
        return this;
    }

    public final boolean isVisible() {
        return visible;
    }

    public GuiElement setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public final boolean isEnabled() {
        return visible && enabled;
    }

    public GuiElement setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean contains(Point2 p) {
        return adjustedPos.x() <= p.x() && p.x() <= adjustedPos.x() + width
               && adjustedPos.y() <= p.y() && p.y() <= adjustedPos.y() + height;
    }

    /**
     * Called when this element is clicked. This should be called from the parent {@code Screen}.
     *
     * @param event the mouse click event
     */
    public void onClick(MouseButtonEvent event) {
        // By default, do nothing
    }

    private void updateAdjustedPos() {
        final double xShift = horizAlign.leftAdjustment(width);
        final double yShift = vertAlign.topAdjustment(height);
        adjustedPos = pos.plus(xShift, yShift);
    }
}
