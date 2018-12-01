package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;

public class AccessorSetUpEvent extends AccessorEvent<AccessorSetUpEvent> {

    private static final HandlerList<AccessorSetUpEvent> HANDLER_LIST = new HandlerList<>();

    public AccessorSetUpEvent(DatabaseTypeAccessor<?> accessor) {
        super(accessor);
    }

    @Override
    public HandlerList<AccessorSetUpEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorSetUpEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
