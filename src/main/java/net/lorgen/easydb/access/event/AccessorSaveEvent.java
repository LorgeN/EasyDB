package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;

public class AccessorSaveEvent extends QueryEvent {

    public AccessorSaveEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor, query);
    }
}
