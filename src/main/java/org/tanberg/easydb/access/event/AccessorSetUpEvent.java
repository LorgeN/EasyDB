package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;

public class AccessorSetUpEvent extends AccessorEvent {

    public AccessorSetUpEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }
}
