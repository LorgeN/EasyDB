package net.lorgen.easydb.query.req;

import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.query.Operator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

public class SimpleRequirement implements QueryRequirement {

    private PersistentField<?> field;
    private Operator operator;
    private Object value; // Store as an object, and when serialized we use the field serializer

    public SimpleRequirement(PersistentField<?> field, Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        if (!this.field.getType().matches(value.getClass())) {
            throw new IllegalArgumentException("Not a valid value!");
        }

        this.value = value;
    }

    public PersistentField<?> getField() {
        return field;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleRequirement that = (SimpleRequirement) o;
        return Objects.equals(field, that.field) &&
          operator == that.operator &&
          Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, operator, value);
    }

    @Override
    public String toString() {
        return "SimpleRequirement{" +
          "field=" + field +
          ", operator=" + operator +
          ", value=" + value +
          '}';
    }
}
