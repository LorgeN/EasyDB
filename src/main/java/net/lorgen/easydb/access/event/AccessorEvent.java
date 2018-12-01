package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.Event;

/**
 * An event.
 *
 * @param <T> This event as a type (E. g. {@code ExampleEvent extends Event<ExampleEvent>})
 */
public abstract class AccessorEvent<T extends AccessorEvent<T>> extends Event<T> {

    private DatabaseTypeAccessor<?> accessor;

    public AccessorEvent(DatabaseTypeAccessor<?> accessor) {
        this.accessor = accessor;
    }

    public DatabaseTypeAccessor<?> getAccessor() {
        return accessor;
    }
}
