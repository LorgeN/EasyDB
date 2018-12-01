package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.query.Query;

public abstract class QueryEvent<T extends QueryEvent<T>> extends AccessorEvent<T> {

    private Query<?> query;

    public QueryEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor);
        this.query = query;
    }

    public Query<?> getQuery() {
        return query;
    }
}
