package net.lorgen.easydb.query;

import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;

import java.util.Arrays;

public interface ValueHolder<T> {

    FieldValue<T>[] getValues();

    default FieldValue<T> getValue(String field) {
        return Arrays.stream(this.getValues())
          .filter(value -> value.getField().getName().equalsIgnoreCase(field))
          .findFirst().orElse(null);
    }

    default FieldValue<T> getValue(PersistentField<T> field) {
        return Arrays.stream(this.getValues())
          .filter(value -> value.getField().equals(field))
          .findFirst().orElse(null);
    }

    default boolean hasValue(PersistentField<T> field) {
        return Arrays.stream(this.getValues()).anyMatch(value -> value.getField().equals(field));
    }
}
