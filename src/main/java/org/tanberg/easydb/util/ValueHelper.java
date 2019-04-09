package org.tanberg.easydb.util;

import org.tanberg.easydb.field.FieldValue;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.query.Operator;
import org.tanberg.easydb.util.reflection.UtilType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class ValueHelper {

    public static void eliminateUnmatching(Collection<ValueContainer> collection, int index, Object value, Operator operator) {
        collection.removeIf(container -> !matches(container.getValues()[index], value, operator));
    }

    public static <T> FieldValue<T> getValue(FieldValue<T>[] values, String field) {
        return Arrays.stream(values)
          .filter(value -> value.getField().getName().equalsIgnoreCase(field))
          .findFirst().orElse(null);
    }

    public static <T> FieldValue<T> getValue(FieldValue<T>[] values, PersistentField<T> field) {
        return Arrays.stream(values)
          .filter(value -> value.getField().equals(field))
          .findFirst().orElse(null);
    }

    public static <T> boolean hasValue(FieldValue<T>[] values, PersistentField<T> field) {
        return Arrays.stream(values).anyMatch(value -> value.getField().equals(field));
    }

    public static boolean matches(Object value1, Object value2, Operator operator) {
        switch (operator) {
            case NOT_EQUALS:
                return !Objects.equals(value1, value2);
            case EQUALS:
                return Objects.equals(value1, value2);
            case LESS_THAN:
                UtilType.assertNumbers(value1, value2);
                return UtilCompare.compare((Number) value1, (Number) value2) < 0;
            case LESS_THAN_OR_EQUAL_TO:
                UtilType.assertNumbers(value1, value2);
                return UtilCompare.compare((Number) value1, (Number) value2) <= 0;
            case GREATER_THAN:
                UtilType.assertNumbers(value1, value2);
                return UtilCompare.compare((Number) value1, (Number) value2) > 0;
            case GREATER_THAN_OR_EQUAL_TO:
                UtilType.assertNumbers(value1, value2);
                return UtilCompare.compare((Number) value1, (Number) value2) >= 0;
            default:
                throw new UnsupportedOperationException("Unsupported operator " + operator.name());
        }
    }
}
