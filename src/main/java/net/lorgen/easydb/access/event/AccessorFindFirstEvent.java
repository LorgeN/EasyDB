package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.response.ResponseEntity;

public class AccessorFindFirstEvent extends QueryEvent<AccessorFindFirstEvent> {

    private static final HandlerList<AccessorFindFirstEvent> HANDLER_LIST = new HandlerList<>();

    private ResponseEntity<?> response;

    public AccessorFindFirstEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, ResponseEntity<?> response) {
        super(accessor, query);
        this.response = response;
    }

    public ResponseEntity<?> getResponse() {
        return response;
    }

    @Override
    public HandlerList<AccessorFindFirstEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorFindFirstEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
