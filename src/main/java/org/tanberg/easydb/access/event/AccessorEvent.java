package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.event.Event;

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
