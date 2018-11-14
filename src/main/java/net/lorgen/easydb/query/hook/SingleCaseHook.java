package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public interface SingleCaseHook<T extends StoredItem> {

    void call(Query<T> query);
}
