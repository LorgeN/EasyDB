package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public class FindHookWrapper<T extends StoredItem> implements InteractionHook<T> {

    private SingleCaseHook<T> hook;

    public FindHookWrapper(SingleCaseHook<T> hook) {
        this.hook = hook;
    }

    @Override
    public void onFind(Query<T> query) {
        this.hook.call(query);
    }
}
