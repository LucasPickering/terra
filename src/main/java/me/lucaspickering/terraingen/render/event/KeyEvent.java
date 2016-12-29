package me.lucaspickering.terraingen.render.event;

public class KeyEvent extends Event {

    public final int key;
    public final int scancode;
    public final int action;
    public final int mods;

    public KeyEvent(long window, int key, int scancode, int action, int mods) {
        super(window);
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }
}
