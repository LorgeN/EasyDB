package net.lorgen.easydb;

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
}
