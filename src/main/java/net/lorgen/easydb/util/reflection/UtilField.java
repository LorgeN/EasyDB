package net.lorgen.easydb.util.reflection;

import java.lang.reflect.Field;

public class UtilField {

    public static <T> Field getField(Class<T> tClass, String field) {
        try {
            return tClass.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
}
