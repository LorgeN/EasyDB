package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;

public class AccessorSetUpEvent extends AccessorEvent {

    public AccessorSetUpEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }
}
