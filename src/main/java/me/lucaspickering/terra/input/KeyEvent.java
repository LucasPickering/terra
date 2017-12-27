package me.lucaspickering.terra.input;

public class KeyEvent extends Event {

    public final Command command;
    public final ButtonAction action;
    public final int mods;

    public KeyEvent(long window, Command command, ButtonAction action, int mods) {
        super(window);
        this.command = command;
        this.action = action;
        this.mods = mods;
    }

    public KeyEvent(long window, Command command, int action, int mods) {
        this(window, command, ButtonAction.getByGlfwCode(action), mods);
    }
}
