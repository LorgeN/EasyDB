package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.event.HandlerList;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.response.Response;

import java.util.List;

public class AccessorFindAllEvent extends QueryEvent<AccessorFindAllEvent> {

    private static final HandlerList<AccessorFindAllEvent> HANDLER_LIST = new HandlerList<>();

    private List<Response<?>> responses;

    public AccessorFindAllEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, List<Response<?>> responses) {
        super(accessor, query);
        this.responses = responses;
    }

    public List<Response<?>> getResponses() {
        return responses;
    }

    @Override
    public HandlerList<AccessorFindAllEvent> getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList<AccessorFindAllEvent> getHandlerList() {
        return HANDLER_LIST;
    }
}
