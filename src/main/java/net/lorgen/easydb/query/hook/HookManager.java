package net.lorgen.easydb.query.hook;

import com.google.common.collect.Lists;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

import java.util.Collections;
import java.util.List;

public class HookManager<T extends StoredItem> {

    private List<InteractionHook<T>> hooks;

    public HookManager() {
        this.hooks = Lists.newArrayList();
    }

    @SafeVarargs
    public final void registerHooks(InteractionHook<T>... hooks) {
        Collections.addAll(this.hooks, hooks);
    }

    public void onSave(SingleCaseHook<T> hook) {
        this.registerHooks(new SaveHookWrapper<>(hook));
    }

    public void onFind(SingleCaseHook<T> hook) {
        this.registerHooks(new FindHookWrapper<>(hook));
    }

    public void onDelete(SingleCaseHook<T> hook) {
        this.registerHooks(new DeleteHookWrapper<>(hook));
    }

    public void onInject(SingleCaseHook<T> hook) {
        this.registerHooks(new InjectHookWrapper<>(hook));
    }

    public void callSaveHooks(Query<T> query) {

    }

    public void callFindHooks(Query<T> query) {

    }

    public void callDeleteHooks(Query<T> query) {

    }

    public void callInjectHooks(Query<T> query) {

    }
}
