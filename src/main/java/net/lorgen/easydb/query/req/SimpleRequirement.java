package net.lorgen.easydb.query.req;

import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.query.Operator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        if (this == o) return true;

        if (!(o instanceof SimpleRequirement)) return false;

        SimpleRequirement that = (SimpleRequirement) o;

        return new EqualsBuilder()
          .append(field, that.field)
          .append(operator, that.operator)
          .append(value, that.value)
          .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(field)
          .append(operator)
          .append(value)
          .toHashCode();
    }

    @Override
    public String toString() {
        return "SimpleRequirement{" + field.getName() + "@" + field.getTypeClass() + " " +
          operator.name() + " " + value + '}';
    }
}
