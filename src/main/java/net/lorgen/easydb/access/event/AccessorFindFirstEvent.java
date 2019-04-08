package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.response.Response;

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
