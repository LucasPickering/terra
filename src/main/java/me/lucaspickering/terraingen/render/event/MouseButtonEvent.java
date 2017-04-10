package me.lucaspickering.terraingen.render.event;

import me.lucaspickering.utils.Point;

public class MouseButtonEvent extends Event {

    public final int button;
    public final int action;
    public final int mods;
    public final Point mousePos;

    public MouseButtonEvent(long window, int button, int action, int mods, Point mousePos) {
        super(window);
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.mousePos = mousePos;
    }
}
