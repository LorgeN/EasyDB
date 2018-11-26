package net.lorgen.easydb.query.hook;

import com.google.common.collect.Lists;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HookManager<T> {

    private List<InteractionHook<T>> hooks;

    public HookManager() {
        this.hooks = Lists.newArrayList();
    }

    @SafeVarargs
    public final void registerHooks(InteractionHook<T>... hooks) {
        Collections.addAll(this.hooks, hooks);
    }

    public void onSave(Consumer<Query<T>> consumer) {
        this.registerHooks(new InteractionHook<T>() {
            @Override
            public void onSave(Query<T> query) {
                consumer.accept(query);
            }
        });
    }

    public void onFind(Consumer<Query<T>> consumer) {
        this.registerHooks(new InteractionHook<T>() {
            @Override
            public void onFind(Query<T> query) {
                consumer.accept(query);
            }
        });
    }

    public void onDelete(Consumer<Query<T>> consumer) {
        this.registerHooks(new InteractionHook<T>() {
            @Override
            public void onDelete(Query<T> query) {
                consumer.accept(query);
            }
        });
    }

    public void onInject(BiConsumer<T, Query<T>> consumer) {
        this.registerHooks(new InteractionHook<T>() {
            @Override
            public void onInject(T instance, Query<T> query) {
                consumer.accept(instance, query);
            }
        });
    }

    public void callSaveHooks(Query<T> query) {
        this.hooks.forEach(tInteractionHook -> tInteractionHook.onSave(query));
    }

    public void callFindHooks(Query<T> query) {
        this.hooks.forEach(tInteractionHook -> tInteractionHook.onFind(query));
    }

    public void callDeleteHooks(Query<T> query) {
        this.hooks.forEach(tInteractionHook -> tInteractionHook.onDelete(query));
    }

    public void callInjectHooks(T instance, Query<T> query) {

    }
}
