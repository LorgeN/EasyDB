package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;

public class AccessorDropEvent extends AccessorEvent<AccessorDropEvent> {

    private static final HandlerList<AccessorDropEvent> HANDLER_LIST = new HandlerList<>();

    public AccessorDropEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }

    @Override
    public HandlerList<AccessorDropEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorDropEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
