package org.tanberg.easydb.query;

import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.response.Response;
import org.tanberg.easydb.util.ValueHelper;

/**
 * A holder of {@link FieldValue values} of a specific type, such as a {@link Query} or {@link Response}. Features
 * helpful delegated methods to {@link ValueHelper}.
 *
 * @param <T> The type of the {@link FieldValue values}
 */
public interface ValueHolder<T> {

    /**
     * @return The stored values
     */
    FieldValue<T>[] getValues();

    default FieldValue<T> getValue(String field) {
        return ValueHelper.getValue(this.getValues(), field);
    }

    default FieldValue<T> getValue(PersistentField<T> field) {
        return ValueHelper.getValue(this.getValues(), field);
    }

    default boolean hasValue(PersistentField<T> field) {
        return ValueHelper.hasValue(this.getValues(), field);
    }
}
