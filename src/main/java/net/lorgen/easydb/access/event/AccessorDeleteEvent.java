package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;

public class AccessorDeleteEvent extends QueryEvent<AccessorDeleteEvent> {

    private static final HandlerList<AccessorDeleteEvent> HANDLER_LIST = new HandlerList<>();

    public AccessorDeleteEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor, query);
    }

    @Override
    public HandlerList<AccessorDeleteEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorDeleteEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
