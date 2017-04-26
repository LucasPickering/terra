package me.lucaspickering.terra.input;

public abstract class Event {

    public final long window;

    protected Event(long window) {
        this.window = window;
    }
}
