package net.lorgen.easydb.event;

public abstract class Event<T extends Event<T>> {

    public abstract HandlerList<T> getHandlers();
}
