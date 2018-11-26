package net.lorgen.easydb.query.hook;

import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.Query;

public interface InteractionHook<T> {

    default void onSave(Query<T> query) {
    }

    default void onFind(Query<T> query) {
    }

    default void onDelete(Query<T> query) {
    }

    default void onInject(T instance, Query<T> query) {
    }
}
