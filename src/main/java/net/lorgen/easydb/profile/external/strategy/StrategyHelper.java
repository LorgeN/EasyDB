package net.lorgen.easydb.profile.external.strategy;

import net.lorgen.easydb.Key;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.external.ExternalFieldProfile;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class StrategyHelper {

    public static <T> ExternalFieldProfile<T> getProfiler(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        ProfilerContext context = getContext(field);
        ProfilerStrategy strategy = suggestStrategy(context, field);
        return new ExternalFieldProfile<>(accessor, field, context, strategy);
    }

    public static ProfilerContext getContext(PersistentField<?> field) {
        Class<?> typeClass = field.getTypeClass();
        if (Map.class.isAssignableFrom(typeClass)) {
            return ProfilerContext.MAP;
        }

        if (Collection.class.isAssignableFrom(typeClass)) {
            return ProfilerContext.COLLECTION;
        }

        return ProfilerContext.FIELD;
    }

    public static ProfilerStrategy suggestStrategy(ProfilerContext context, PersistentField<?> field) {
        Class<?> typeClass = field.getTypeClass();

        switch (context) {
            case FIELD:
                if (!hasKeys(typeClass)) {
                    return ProfilerStrategy.DECLARING_KEYS;
                }

                return ProfilerStrategy.DIRECT_USE;
            case COLLECTION:
                if (!hasKeys(typeClass)) {
                    return ProfilerStrategy.DECLARING_KEYS_WITH_INDEX;
                }

                return ProfilerStrategy.DIRECT_USE;
            case MAP:
                return ProfilerStrategy.DECLARING_KEYS_WITH_INDEX;
        }

        throw new IllegalArgumentException("Unknown scenario! No suggestion possible.");
    }

    private static boolean hasKeys(Class<?> typeClass) {
        for (Field declaredField : typeClass.getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(Key.class)) {
                continue;
            }

            return true;
        }

        return false;
    }

}
