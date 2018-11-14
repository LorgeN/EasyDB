package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public class DeleteHookWrapper<T extends StoredItem> implements InteractionHook<T> {

    private SingleCaseHook<T> hook;

    public DeleteHookWrapper(SingleCaseHook<T> hook) {
        this.hook = hook;
    }

    @Override
    public void onDelete(Query<T> query) {
        this.hook.call(query);
    }
}
