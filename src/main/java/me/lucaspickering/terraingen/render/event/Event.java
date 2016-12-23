package me.lucaspickering.terraingen.render.event;

public abstract class Event {

    public final long window;

    Event(long window) {
        this.window = window;
    }
}
