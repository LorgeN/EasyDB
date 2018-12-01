package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;

public class AccessorSaveEvent extends QueryEvent<AccessorSaveEvent> {

    private static final HandlerList<AccessorSaveEvent> HANDLER_LIST = new HandlerList<>();

    public AccessorSaveEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor, query);
    }

    @Override
    public HandlerList<AccessorSaveEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorSaveEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
