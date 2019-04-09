package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.query.Query;
import org.tanberg.easydb.query.response.Response;

public class AccessorFindFirstEvent extends QueryEvent {

    private Response<?> response;

    public AccessorFindFirstEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, Response<?> response) {
        super(accessor, query);
        this.response = response;
    }

    public Response<?> getResponse() {
        return response;
    }
}
