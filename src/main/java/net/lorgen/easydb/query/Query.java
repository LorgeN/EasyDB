package net.lorgen.easydb.query;

import net.lorgen.easydb.FieldValue;
import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.query.req.QueryRequirement;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Query<T extends StoredItem> {

    private Class<T> typeClass;
    private Optional<T> instance;
    private FieldValue<T>[] values;

    // Requirements are "recursive", i. e. we combine the requirements
    // into a single object, which then branches on to the other ones
    private QueryRequirement req;

    // TODO: Limit/order functionality?

    public Query(Class<T> typeClass, T instance, FieldValue<T>[] values, QueryRequirement req) {
        this.typeClass = typeClass;
        this.instance = Optional.of(instance);
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

    public Optional<T> getObjectInstance() {
        return instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Query<?> query = (Query<?>) o;
        return Objects.equals(typeClass, query.typeClass) &&
          Objects.equals(instance, query.instance) &&
          Arrays.equals(values, query.values) &&
          Objects.equals(req, query.req);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeClass, instance, req);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        return "Query{" +
          "typeClass=" + typeClass +
          ", instance=" + instance +
          ", values=" + Arrays.toString(values) +
          ", req=" + req +
          '}';
    }
}
