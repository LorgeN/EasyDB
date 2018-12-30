package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;

public class AccessorDeleteEvent extends QueryEvent {

    public AccessorDeleteEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor, query);
    }
}
