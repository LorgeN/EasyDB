package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;

public class AccessorDropEvent extends AccessorEvent {

    public AccessorDropEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }
}
