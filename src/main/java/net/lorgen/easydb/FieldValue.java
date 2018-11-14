package net.lorgen.easydb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

public class FieldValue<T extends StoredItem> {

    private PersistentField<T> field;
    private Object value;

    public FieldValue(PersistentField<T> field, Object value) {
        this.field = field;
        if (!this.field.getType().matches(value.getClass())) {
            throw new IllegalArgumentException("Not a valid value!");
        }

        this.value = value;
    }

    public PersistentField<T> getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (!this.field.getType().matches(value.getClass())) {
            throw new IllegalArgumentException("Not a valid value!");
        }

        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldValue<?> that = (FieldValue<?>) o;
        return Objects.equals(field, that.field) &&
          Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value);
    }

    @Override
    public String toString() {
        return "FieldValue{" +
          "field=" + field +
          ", value=" + value +
          '}';
    }
}
