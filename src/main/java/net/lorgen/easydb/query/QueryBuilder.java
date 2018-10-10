package net.lorgen.easydb.query;

import net.lorgen.easydb.FieldValue;
import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.RequirementBuilder;

import java.util.Arrays;

public class QueryBuilder<T extends StoredItem> {

    private StorageManager<T> manager;
    private T instance; // If we wish to pass an instance, we can

    // We could consider using a list here, but as this is what e. g. an
    // array list does internally, there is no performance benefit.
    // We don't pass a type param here because Java doesn't like type params and arrays
    private FieldValue[] values;
    private QueryRequirement req;

    public QueryBuilder(StorageManager<T> manager) {
        this.manager = manager;
        this.values = new FieldValue[0];
    }

    public QueryBuilder<T> set(T object) {
        this.instance = object;

        // Even tho we pass an instance of T, we still assign the values as we wish to use
        // them instead of querying upon the instance every time we need a value
        PersistentField<T>[] keys = this.manager.getProfile().getKeys();
        for (PersistentField<T> key : keys) {
            this.set(key, key.get(object));
        }

        return this;
    }

    public QueryBuilder<T> set(String fieldName, Object value) {
        return this.set(this.resolve(fieldName), value);
    }

    public QueryBuilder<T> set(PersistentField<T> field, Object value) {
        FieldValue<T> fieldValue = this.getValue(field);
        if (fieldValue != null) {
            fieldValue.setValue(value);
            return this;
        }

        fieldValue = new FieldValue<>(field, value);
        this.values = Arrays.copyOf(this.values, this.values.length + 1);
        this.values[this.values.length - 1] = fieldValue;
        return this;
    }

    public RequirementBuilder<T> where() {
        return new RequirementBuilder<>(this.manager, this);
    }

    public QueryBuilder<T> setRequirement(QueryRequirement req) {
        this.req = req;
        return this;
    }

    public Query<T> build() {
        return new Query<>(this.manager.getTypeClass(), this.instance, this.values, this.req);
    }

    // Shortcut methods

    public void deleteSync() {

    }

    public void deleteAsync() {
        this.deleteAsync(null);
    }

    public void deleteAsync(Runnable callback) {

    }

    public void saveSync() {

    }

    public void saveAsync() {
        this.saveAsync(null);
    }

    public void saveAsync(Runnable callback) {

    }

    // Internals

    private PersistentField<T> resolve(String name) {
        PersistentField<T> field = this.manager.getProfile().resolveField(name);
        if (field == null) {
            throw new IllegalArgumentException("Couldn't find field \"" + name + "\" in class " +
              this.manager.getTypeClass().getSimpleName() + "!");
        }

        return field;
    }

    private FieldValue<T> getValue(PersistentField<T> field) {
        return Arrays.stream(this.values)
          .filter(value -> value.getField().equals(field))
          .findFirst().orElse(null);
    }

    private boolean hasValue(PersistentField<T> field) {
        return Arrays.stream(this.values).anyMatch(value -> value.getField().equals(field));
    }
}
