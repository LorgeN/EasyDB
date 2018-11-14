package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public class SaveHookWrapper<T extends StoredItem> implements InteractionHook<T> {

    private SingleCaseHook<T> hook;

    public SaveHookWrapper(SingleCaseHook<T> hook) {
        this.hook = hook;
    }

    @Override
    public void onSave(Query<T> query) {
        this.hook.call(query);
    }
}
