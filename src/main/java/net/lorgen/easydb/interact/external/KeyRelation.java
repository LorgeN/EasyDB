package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.query.req.RequirementBuilder;

import java.util.Arrays;

public class KeyRelation {

    private KeyHolder holder;
    private String nameDeclaringClass;
    private String nameStoredClass;

    public KeyRelation(KeyHolder holder, String nameDeclaringClass, String nameStoredClass) {
        this.holder = holder;
        this.nameDeclaringClass = nameDeclaringClass;
        this.nameStoredClass = nameStoredClass;
    }

    public KeyHolder getHolder() {
        return holder;
    }

    public String getNameDeclaringClass() {
        return nameDeclaringClass;
    }

    public String getNameStoredClass() {
        return nameStoredClass;
    }

    public <T> RequirementBuilder<T> append(RequirementBuilder<T> builder, FieldValue<T>[] values) {
        FieldValue<T> value = this.getValue(values);
        if (value == null) {
            throw new IllegalArgumentException("Couldn't find value for field \"" + this.getNameDeclaringClass() + "\"!");
        }

        return builder.andEquals(this.nameStoredClass, value.getValue());
    }

    public <T> FieldValue<T> getValue(FieldValue<T>[] values) {
        return Arrays.stream(values)
          .filter(value -> value.getField().getName().equalsIgnoreCase(this.nameDeclaringClass))
          .findFirst().orElse(null);
    }

    public enum KeyHolder {
        ADDED_INDEX,
        DECLARING_VALUE
    }
}
