package net.lorgen.easydb;

import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.response.Response;
import net.lorgen.easydb.util.Callback;
import net.lorgen.easydb.util.concurrency.UtilConcurrency;

import java.util.Arrays;
import java.util.List;

/**
 * A repository containing something (Normally an object T). Each repository
 * is responsible for managing a single table. This table may however be stored
 * in multiple places, or not at all.
 *
 * @param <T> The type this repository contains. If there is no type,
 *            and the entire repository is just built using builders,
 *            you can simply set this to the {@link Object} class.
 */
public interface ItemRepository<T> {

    String getTableName();

    Class<T> getTypeClass();

    ItemProfile<T> getProfile();

    boolean isSearchable(PersistentField<T> field);

    default QueryBuilder<T> newQuery() {
        return new QueryBuilder<>(this);
    }

    default void findFirstAsync(Query<T> query, Callback<Response<T>> callback) {
        UtilConcurrency.submit(() -> {
            Response<T> value = this.findFirstSync(query);
            callback.call(value);
        });
    }

    Response<T> findFirstSync(Query<T> query);

    default void findAllAsync(Query<T> query, Callback<List<Response<T>>> callback) {
        UtilConcurrency.submit(() -> {
            List<Response<T>> value = this.findAllSync(query);
            callback.call(value);
        });
    }

    List<Response<T>> findAllSync(Query<T> query);

    default void saveAsync(T object) {
        this.saveAsync(this.newQuery()
          .set(object)
          .build());
    }

    default void saveAsync(Query<T> query) {
        this.saveAsync(query, null);
    }

    default void saveAsync(Query<T> query, Runnable callback) {
        UtilConcurrency.submit(() -> {
            this.saveSync(query);
            if (callback == null) {
                return;
            }

            callback.run();
        });
    }

    default void saveSync(T object) {
        this.saveSync(this.newQuery()
          .set(object)
          .build());
    }

    void saveSync(Query<T> query);

    default void deleteAsync(T object) {
        this.deleteAsync(this.newQuery()
          .where()
          .andKeysAre(object)
          .closeAll()
          .build());
    }

    default void deleteAsync(Query<T> query) {
        this.deleteAsync(query, null);
    }

    default void deleteAsync(Query<T> query, Runnable callback) {
        UtilConcurrency.submit(() -> {
            this.deleteSync(query);
            if (callback == null) {
                return;
            }

            callback.run();
        });
    }

    default void deleteSync(T object) {
        this.deleteSync(this.newQuery()
          .where()
          .andKeysAre(object)
          .closeAll()
          .build());
    }

    void deleteSync(Query<T> query);

    default void updateArrayValue(PersistentField<T> field, Object value, FieldValue<T>[] values) {
        Arrays.stream(values)
          .filter(fieldValue -> fieldValue.getField().equals(field))
          .forEach(fieldValue -> fieldValue.setValue(value));
    }

    default Object getArrayValue(PersistentField<T> field, FieldValue<T>[] values) {
        return Arrays.stream(values)
          .filter(value -> value.getField().equals(field))
          .map(FieldValue::getValue)
          .findFirst().orElse(null);
    }
}
