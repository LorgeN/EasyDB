package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

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
