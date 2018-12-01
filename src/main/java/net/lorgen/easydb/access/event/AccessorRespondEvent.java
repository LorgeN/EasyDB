package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.response.ResponseEntity;

public class AccessorRespondEvent extends QueryEvent<AccessorRespondEvent> {

    private static final HandlerList<AccessorRespondEvent> HANDLER_LIST = new HandlerList<>();

    private ResponseEntity<?> entity;

    public AccessorRespondEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, ResponseEntity<?> entity) {
        super(accessor, query);
        this.entity = entity;
    }

    public ResponseEntity<?> getEntity() {
        return entity;
    }

    @Override
    public HandlerList<AccessorRespondEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorRespondEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}