package me.lucaspickering.terra.input;

import me.lucaspickering.utils.Point;

public class MouseButtonEvent extends Event {

    public final int button;
    public final ButtonAction action;
    public final int mods;
    public final Point mousePos;

    public MouseButtonEvent(long window, int button, ButtonAction action, int mods,
                            Point mousePos) {
        super(window);
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.mousePos = mousePos;
    }
}
