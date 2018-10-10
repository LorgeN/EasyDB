package net.lorgen.easydb.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class UtilType {

    public static final List<Class<?>> PRIMITIVE_TYPES = ImmutableList.of(
      Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE,
      Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class
    );

    public static boolean isPrimitive(Class<?> tClass) {
        return PRIMITIVE_TYPES.contains(tClass);
    }
}
