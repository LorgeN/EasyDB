package org.tanberg.easydb.util.reflection;

import java.lang.reflect.Field;

public class UtilField {

    public static boolean hasField(Class<?> tClass, String field) {
        try {
            tClass.getDeclaredField(field);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public static <T> Field getField(Class<T> tClass, String field) {
        try {
            return tClass.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
}
