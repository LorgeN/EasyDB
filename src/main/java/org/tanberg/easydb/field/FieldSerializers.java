package org.tanberg.easydb.field;

import com.google.common.collect.Maps;
import org.tanberg.easydb.DataType;

import java.util.Map;

public class FieldSerializers {

    private static final Map<Class<? extends FieldSerializer>, FieldSerializer> INSTANCE_CACHE = Maps.newConcurrentMap();

    public static <T extends FieldSerializer> T getSerializer(Class<T> tClass) {
        if (tClass == DataType.class) {
            throw new IllegalArgumentException("Can not get serializer DataType!");
        }

        return (T) INSTANCE_CACHE.computeIfAbsent(tClass, someClass -> {
            try {
                return someClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
