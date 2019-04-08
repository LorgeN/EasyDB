package net.lorgen.easydb;

import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.query.response.Response;

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

    Response<T> findFirst(Query<T> query);

    List<Response<T>> findAll(Query<T> query);

    default void save(T object) {
        this.save(this.newQuery()
          .set(object)
          .build());
    }

    void save(Query<T> query);

    default void delete(T object) {
        this.delete(this.newQuery()
          .where()
          .andKeysAreSameAs(object)
          .closeAll()
          .build());
    }

    void delete(Query<T> query);

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
