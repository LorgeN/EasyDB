package org.tanberg.easydb;

import org.tanberg.easydb.field.PersistentField;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class WrappedIndex<T> {

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

    public boolean areFields(Collection<PersistentField<T>> fields) {
        if (fields.size() != this.fields.length) {
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

    public boolean containsFields(Collection<PersistentField<T>> fields) {
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
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WrappedIndex<?> that = (WrappedIndex<?>) o;
        return id == that.id &&
          unique == that.unique &&
          Arrays.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, unique);
        result = 31 * result + Arrays.hashCode(fields);
        return result;
    }

    @Override
    public String toString() {
        return "WrappedIndex{" +
          "id=" + id +
          ", unique=" + unique +
          ", fields=" + Arrays.toString(fields) +
          '}';
    }
}
