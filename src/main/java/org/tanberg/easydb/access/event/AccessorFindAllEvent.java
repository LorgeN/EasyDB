package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

import java.util.List;

public class AccessorFindAllEvent extends QueryEvent {

    private List<Response<?>> responses;

    public AccessorFindAllEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, List<Response<?>> responses) {
        super(accessor, query);
        this.responses = responses;
    }

    public List<Response<?>> getResponses() {
        return responses;
    }
}
