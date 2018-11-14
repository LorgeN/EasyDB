package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public class InjectHookWrapper<T extends StoredItem> implements InteractionHook<T> {

    private SingleCaseHook<T> hook;

    public InjectHookWrapper(SingleCaseHook<T> hook) {
        this.hook = hook;
    }

    @Override
    public void onInject(Query<T> query) {
        this.hook.call(query);
    }
}
