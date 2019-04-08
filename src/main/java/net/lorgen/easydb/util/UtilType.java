package net.lorgen.easydb.util;

import com.google.common.collect.ImmutableList;
import net.lorgen.easydb.DataType;

import java.util.List;
import java.util.UUID;

public class UtilType {

    public static final List<Class<?>> PRIMITIVE_TYPES = ImmutableList.of(
      Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE,
      Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class
    );

    /**
     * Checks if the given {@link Class} is a primitive.
     *
     * @param tClass The {@link Class}
     * @return If the given class is a primitive
     */
    public static boolean isPrimitive(Class<?> tClass) {
        return PRIMITIVE_TYPES.contains(tClass);
    }

    /**
     * Checks if object of the given {@link Class} should be stored in a single
     * field on the database. This is different from {@link #isPrimitive(Class)}
     * in the way that some objects have very simple {@link String} representations,
     * such as {@link UUID} being very easily stored as a string in a single field,
     * as compared to the alternative two fields that are used within the actual
     * class.
     * <p>
     * In a lot of cases we simply refer to this as a primitive field, but that is
     * slightly inaccurate, but does simplify a lot of documentation.
     *
     * @param tClass The {@link Class}
     * @return If this field can be efficiently stored in a single field
     */
    public static boolean shouldBeStoredInSingleField(Class<?> tClass) {
        DataType type = DataType.resolve(tClass);
        return type != null;
    }
}
