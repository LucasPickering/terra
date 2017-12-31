package me.lucaspickering.terra.input;

public class KeyEvent extends Event {

    public final Command command;
    public final ButtonAction action;
    public final int mods;

    public KeyEvent(Command command, ButtonAction action, int mods) {
        this.command = command;
        this.action = action;
        this.mods = mods;
    }

    public KeyEvent(Command command, int action, int mods) {
        this(command, ButtonAction.getByGlfwCode(action), mods);
    }
}
