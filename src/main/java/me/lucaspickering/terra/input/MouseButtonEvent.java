package me.lucaspickering.terra.input;

import me.lucaspickering.utils.Point2;

public class MouseButtonEvent extends Event {

    public final int button;
    public final ButtonAction action;
    public final int mods;
    public final Point2 mousePos;

    public MouseButtonEvent(int button, ButtonAction action, int mods, Point2 mousePos) {
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.mousePos = mousePos;
    }

    public MouseButtonEvent(int button, int action, int mods, Point2 mousePos) {
        this(button, ButtonAction.getByGlfwCode(action), mods, mousePos);
    }
}
