package net.lorgen.easydb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

public class WrappedIndex<T extends StoredItem> {

    private int id;
    private boolean unique;
    private PersistentField<T>[] fields;

    @SafeVarargs
    public WrappedIndex(int id, boolean unique, PersistentField<T>... fields) {
        this.id = id;
        this.unique = unique;
        this.fields = fields;
    }

    public int getId() {
        return id;
    }

    public boolean isUnique() {
        return unique;
    }

    public PersistentField<T>[] getFields() {
        return fields;
    }

    public boolean isField(PersistentField<T> field) {
        for (PersistentField<T> someField : this.getFields()) {
            if (!someField.equals(field)) {
                continue;
            }

            return true;
        }

        return false;
    }

    @SafeVarargs
    public final boolean areFields(PersistentField<T>... fields) {
        if (fields.length != this.fields.length) {
            return false;
        }

        return this.containsFields(fields);
    }

    @SafeVarargs
    public final boolean containsFields(PersistentField<T>... fields) {
        for (PersistentField<T> field : fields) {
            if (this.isField(field)) {
                continue;
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof WrappedIndex)) return false;

        WrappedIndex<?> that = (WrappedIndex<?>) o;

        return new EqualsBuilder()
          .append(id, that.id)
          .append(fields, that.fields)
          .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(id)
          .append(fields)
          .toHashCode();
    }

    @Override
    public String toString() {
        return "WrappedIndex{" +
          "id=" + id +
          ", fields=" + Arrays.toString(fields) +
          '}';
    }
}
