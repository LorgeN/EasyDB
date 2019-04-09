package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;

public class AccessorDropEvent extends AccessorEvent {

    public AccessorDropEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }
}
