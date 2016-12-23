package me.lucaspickering.terraingen.render.event;

import me.lucaspickering.terraingen.util.Point;

public class MouseButtonEvent extends Event {

    public final int button;
    public final int mods;
    public final Point mousePos;

    public MouseButtonEvent(long window, int button, int mods, Point mousePos) {
        super(window);
        this.button = button;
        this.mods = mods;
        this.mousePos = mousePos;
    }

}
