package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.Event;

/**
 * An event
 */
public abstract class AccessorEvent extends Event {

    private DatabaseTypeAccessor<?> accessor;

    public AccessorEvent(DatabaseTypeAccessor<?> accessor) {
        this.accessor = accessor;
    }

    public DatabaseTypeAccessor<?> getAccessor() {
        return accessor;
    }
}
