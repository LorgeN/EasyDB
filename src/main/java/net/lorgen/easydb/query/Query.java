package net.lorgen.easydb.query;

import net.lorgen.easydb.FieldValue;
import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.req.QueryRequirement;

import java.util.Arrays;

public class Query<T extends StoredItem> {

    private Class<T> typeClass;
    private T instance;
    private FieldValue<T>[] values;

    // Requirements are "recursive", i. e. we combine the requirements
    // into a single object, which then branches on to the other ones
    private QueryRequirement req;

    // TODO: Limit/order functionality?

    public Query(Class<T> typeClass, T instance, FieldValue<T>[] values, QueryRequirement req) {
        this.typeClass = typeClass;
        this.instance = instance;
        this.values = values;
        this.req = req;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    public FieldValue<T>[] getValues() {
        return values;
    }

    public QueryRequirement getRequirement() {
        return req;
    }

    public FieldValue<T> getValue(PersistentField<T> field) {
        return Arrays.stream(this.values)
          .filter(value -> value.getField().equals(field))
          .findFirst().orElse(null);
    }

    public boolean hasValue(PersistentField<T> field) {
        return Arrays.stream(this.values).anyMatch(value -> value.getField().equals(field));
    }

    @Override
    public String toString() {
        return "Query{" +
          "typeClass=" + typeClass +
          ", values=" + Arrays.toString(values) +
          ", req=" + req +
          '}';
    }

    public T getInstance() {
        return instance;
    }
}
