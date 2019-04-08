package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.response.Response;

public class AccessorRespondEvent extends QueryEvent {

    private Response<?> entity;

    public AccessorRespondEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, Response<?> entity) {
        super(accessor, query);
        this.entity = entity;
    }

    public Response<?> getEntity() {
        return entity;
    }
}
