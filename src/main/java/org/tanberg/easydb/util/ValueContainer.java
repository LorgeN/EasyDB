package org.tanberg.easydb.util;

import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.ItemProfile;

import java.util.Arrays;

public class ValueContainer {

    public static <T> ValueContainer getKeys(ItemProfile<T> profile, FieldValue<T>[] values) {
        PersistentField<T>[] keys = profile.getKeys();
        return getValues(keys, values);
    }

    public static <T> ValueContainer getValues(PersistentField<T>[] fields, FieldValue<T>[] values) {
        Object[] valueArray = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            PersistentField<T> key = fields[i];
            FieldValue<T> value = ValueHelper.getValue(values, key);
            if (value == null) {
                throw new IllegalArgumentException("Missing key value " + key + "!");
            }

            valueArray[i] = value.getValue();
        }

        return new ValueContainer(valueArray);
    }

    private Object[] values;

    public ValueContainer(Object[] values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValue(int index, Object value) {
        this.values[index] = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ValueContainer)) {
            return false;
        }

        ValueContainer that = (ValueContainer) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "ValueContainer{" +
          "values=" + Arrays.toString(values) +
          '}';
    }
}
