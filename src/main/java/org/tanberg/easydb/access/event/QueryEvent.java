package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.query.Query;

public abstract class QueryEvent extends AccessorEvent {

    private Query<?> query;

    public QueryEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor);
        this.query = query;
    }

    public Query<?> getQuery() {
        return query;
    }
}
