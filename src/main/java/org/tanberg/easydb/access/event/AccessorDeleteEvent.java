package org.tanberg.easydb.access.event;

import org.tanberg.easydb.access.DatabaseTypeAccessor;
import org.tanberg.easydb.query.Query;

public class AccessorDeleteEvent extends QueryEvent {

    public AccessorDeleteEvent(DatabaseTypeAccessor<?> accessor, Query<?> query) {
        super(accessor, query);
    }
}
