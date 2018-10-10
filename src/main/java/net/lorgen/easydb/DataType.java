package net.lorgen.easydb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lorgen.easydb.util.UtilGSON;
import net.lorgen.easydb.util.UtilType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A type of data that we can store in an {@link StoredItem item}.
 */
public enum DataType implements FieldSerializer {
    /**
     * A special case where the method {@link DataType#resolve(Field)} is
     * used to figure out which {@link DataType type} should actually be
     * used.
     * <p>
     * This is used as the default type in the {@link Persist} annotation,
     * and in most cases should be fine to use.
     */
    AUTO() {
        // Throw exceptions so that if something somehow selects auto we can easily
        // see that this is the issue, and follow the stack trace to find the cause

        @Override
        public boolean returnsPrimitive(StorageManager<?> manager, PersistentField<?> field) {
            throw new UnsupportedOperationException("This operation is not supported by the AUTO data type!");
        }

        @Override
        public boolean matches(Class<?> someClass) {
            throw new UnsupportedOperationException("This operation is not supported by the AUTO data type!");
        }

        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            throw new UnsupportedOperationException("This operation is not supported by the AUTO data type!");
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            throw new UnsupportedOperationException("This operation is not supported by the AUTO data type!");
        }
    },
    /**
     * A {@link String}
     */
    STRING(String.class),

    /*
     * In theory we could technically replace all the primitive types in this
     * enum with a single enum constant and using reflection to retrieve the
     * #valueOf method in each of them. This approach will provide cleaner code,
     * and may appear "fancier" to some people, but will in reality be a much
     * slower approach due to the use of reflection or some form of mapped cache.
     * Having these system as fast as possible is very important as they are
     * some of the most central methods in the storage system.
     */

    /**
     * A {@link Byte}, or it's primitive {@code byte}
     */
    BYTE(Byte.class, Byte.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Byte.valueOf(string);
        }
    },
    /**
     * A {@link Short}, or it's primitive {@code short}
     */
    SHORT(Short.class, Short.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Short.valueOf(string);
        }
    },
    /**
     * A {@link Integer}, or it's primitive {@code int}
     */
    INTEGER(Integer.class, Integer.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Integer.valueOf(string);
        }
    },
    /**
     * A {@link Long}, or it's primitive {@code long}
     */
    LONG(Long.class, Long.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Long.valueOf(string);
        }
    },
    /**
     * A {@link Float}, or it's primitive {@code float}
     */
    FLOAT(Float.class, Float.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Float.valueOf(string);
        }
    },
    /**
     * A {@link Double}, or it's primitive {@code double}
     */
    DOUBLE(Double.class, Double.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Double.valueOf(string);
        }
    },
    /**
     * A {@link Boolean}, or it's primitive {@code boolean}
     */
    BOOLEAN(Boolean.class, Boolean.TYPE) {
        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            return Boolean.valueOf(string);
        }
    },

    /*
     * It is worth explicitly mentioning primitives are serializable by GSON, so storing
     * primitives does not require any kind of wrapper or similar.
     */

    /**
     * A {@link List}. Requires a {@link Persist#typeParams()} value for the type
     * parameter of the list. Requires the value to be serializable using {@link Gson GSON}.
     */
    LIST(List.class) {
        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            JsonArray array = new JsonArray();

            List<?> list = (List<?>) val;
            for (Object object : list) {
                array.add(UtilGSON.INSTANCE.toJsonTree(object));
            }

            return UtilGSON.INSTANCE.toJson(array);
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            JsonArray array = UtilGSON.PARSER.parse(string).getAsJsonArray();
            // A list only takes 1 type parameter, anything else means something
            // is wrong somewhere
            if (field.getTypeParameters().length != 1) {
                throw new IllegalArgumentException("Invalid type parameters for field \"" + field.getName() +
                  "\" in " + field.getTypeClass().getSimpleName() + "!");
            }

            // Don't give type params at all to avoid errors. As type parameters
            // are mostly just a convenience thing, this has no effect after the
            // code is compiled
            Class typeClass = field.getTypeParameters()[0];
            // We don't support different types of lists, so we may want to consider
            // something for that in the future
            List list = Lists.newLinkedList();

            for (JsonElement element : array) {
                list.add(UtilGSON.INSTANCE.fromJson(element, typeClass));
            }

            return list;
        }
    },
    /**
     * A {@link Set}. Requires a {@link Persist#typeParams()} value for the type
     * parameter of the set. Requires the value to be serializable using {@link Gson GSON}.
     */
    SET(Set.class) {
        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            JsonArray array = new JsonArray();

            Set<?> list = (Set<?>) val;
            for (Object object : list) {
                array.add(UtilGSON.INSTANCE.toJsonTree(object));
            }

            return UtilGSON.INSTANCE.toJson(array);
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            JsonArray array = UtilGSON.PARSER.parse(string).getAsJsonArray();
            // A set only takes 1 type parameter, anything else means something
            // is wrong somewhere
            if (field.getTypeParameters().length != 1) {
                throw new IllegalArgumentException("Invalid type parameters for field \"" + field.getName() +
                  "\" in " + field.getTypeClass().getSimpleName() + "!");
            }

            // Don't give type params at all to avoid errors. As type parameters
            // are mostly just a convenience thing, this has no effect after the
            // code is compiled
            Class typeClass = field.getTypeParameters()[0];
            // We don't support different types of sets, so we may want to consider
            // something for that in the future
            Set set = Sets.newHashSet();

            for (JsonElement element : array) {
                set.add(UtilGSON.INSTANCE.fromJson(element, typeClass));
            }

            return set;
        }
    },
    /**
     * An array, of any type. Requires the value to be serializable using {@link Gson GSON}.
     */
    ARRAY() {
        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            JsonArray array = new JsonArray();
            Object[] values = (Object[]) val;

            for (Object object : values) {
                array.add(UtilGSON.INSTANCE.toJsonTree(object));
            }

            return UtilGSON.INSTANCE.toJson(array);
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            JsonArray array = UtilGSON.PARSER.parse(string).getAsJsonArray();
            int size = array.size();
            Class typeClass = field.getField().getType().getComponentType();
            Object[] values = (Object[]) Array.newInstance(typeClass, size);

            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                values[i] = UtilGSON.INSTANCE.fromJson(element, typeClass);
            }

            return values;
        }

        @Override
        public boolean matches(Class<?> someClass) {
            return someClass.isArray();
        }
    },
    /**
     * A {@link Map}. Requires two {@link Persist#typeParams()}, where the first one corresponds
     * to the key {@link Type type} of the map, and the second corresponds to the value type. Both
     * types are required to be serializable using {@link Gson GSON}.
     */
    MAP(Map.class) {
        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            Map<?, ?> map = (Map<?, ?>) val;
            JsonObject object = new JsonObject();

            for (Entry<?, ?> entry : map.entrySet()) {
                // We serialize using GSON for simplicity's sake
                object.add(UtilGSON.INSTANCE.toJson(entry.getKey()), UtilGSON.INSTANCE.toJsonTree(entry.getValue()));
            }

            return UtilGSON.INSTANCE.toJson(object);
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            JsonObject object = UtilGSON.PARSER.parse(string).getAsJsonObject();
            // We don't support different types of maps, so we may want to consider
            // something for that in the future
            Map map = Maps.newHashMap();

            Class<?>[] typeParams = field.getTypeParameters();
            if (typeParams.length != 2) {
                throw new IllegalArgumentException("Invalid type parameters for field \"" + field.getName() +
                  "\" in " + field.getTypeClass().getSimpleName() + "!");
            }

            Class keyClass = typeParams[0];
            Class valueClass = typeParams[1];

            for (Entry<String, JsonElement> entry : object.entrySet()) {
                map.put(UtilGSON.INSTANCE.fromJson(entry.getKey(), keyClass), UtilGSON.INSTANCE.fromJson(entry.getValue(), valueClass));
            }

            return map;
        }
    },
    /**
     * An {@link Enum}. Stores using the {@link Enum#ordinal() ordinal} of the enum. This
     * makes it so that if you do change the order of the enum after values have been stored
     * in a database, all existing values will be shifted unless the new enum constant is
     * added to the bottom of the enum.
     */
    ENUM() {
        @Override
        public boolean returnsPrimitive(StorageManager<?> manager, PersistentField<?> field) {
            // We return a simple integer in this case, which is a primitive
            return true;
        }

        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            return String.valueOf(((Enum) val).ordinal());
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            Class<?> typeClass = field.getField().getType();
            return typeClass.getEnumConstants()[Integer.valueOf(string)];
        }

        @Override
        public boolean matches(Class<?> someClass) {
            return someClass.isEnum();
        }
    },
    /**
     * A custom serializer, where none of the serializers in this enum are suitable, and
     * a new enum value can not be added for some reason. This approach will be minimally
     * slower compared to using this enum, but allows for a lot of flexibility.
     * <p>
     * Simply implement {@link FieldSerializer} in a class, leave a no-args constructor
     * and specify it using the {@link Persist#serializer()} field.
     */
    CUSTOM() {
        @Override
        public boolean returnsPrimitive(StorageManager<?> manager, PersistentField<?> field) {
            this.verify(field);

            return FieldSerializers.getSerializer(field.getSerializerClass()).returnsPrimitive(manager, field);
        }

        @Override
        public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
            this.verify(field);

            return FieldSerializers.getSerializer(field.getSerializerClass()).toString(manager, field, val);
        }

        @Override
        public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
            this.verify(field);

            return FieldSerializers.getSerializer(field.getSerializerClass()).fromString(manager, field, string);
        }

        @Override
        public boolean matches(Class<?> someClass) {
            return false;
        }

        private void verify(PersistentField<?> field) {
            if (field.hasCustomSerializer()) {
                return;
            }

            throw new IllegalStateException("Field \"" + field.getName() + "\" in " + field.getTypeClass().getSimpleName() +
              " has CUSTOM data type, but no custom serializer is specified!");
        }
    };

    private boolean primitive;
    private Class<?>[] typeClasses;

    DataType(Class<?>... typeClasses) {
        this.typeClasses = typeClasses;

        if (this.typeClasses.length == 0) {
            this.primitive = false;
            return;
        }

        // We assume that the return type is somewhat similar to the data type
        for (Class<?> typeClass : this.typeClasses) {
            if (UtilType.isPrimitive(typeClass)) {
                continue;
            }

            // This would be a very weird case, but it is not very resource intensive
            // to handle so we do it anyway
            this.primitive = false;
            return;
        }

        this.primitive = true;
    }

    /**
     * Checks if the given class matches this given type, i. e. a field with the given type
     * can use this {@link DataType type} to serialize.
     *
     * @param someClass Some class we want to check if matches this given data type.
     * @return If the class matches
     */
    public boolean matches(Class<?> someClass) {
        for (Class<?> aClass : this.typeClasses) {
            if (!aClass.isAssignableFrom(someClass)) {
                continue;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean returnsPrimitive(StorageManager<?> manager, PersistentField<?> field) {
        return this.primitive;
    }

    @Override
    public String toString(StorageManager<?> manager, PersistentField<?> field, Object val) {
        return String.valueOf(val);
    }

    @Override
    public Object fromString(StorageManager<?> manager, PersistentField<?> field, String string) {
        return string; // In most cases this method will be overwritten
    }

    /**
     * Resolves the appropriate {@link DataType type} for the given {@link Field field}.
     *
     * @param field The {@link Field field}
     * @return The appropriate {@link DataType type}, or {@code null} if no type is found.
     */
    public static DataType resolve(Field field) {
        Class<?> fieldType = field.getType();

        for (DataType type : DataType.values()) {
            if (type == AUTO || !type.matches(fieldType)) {
                continue;
            }

            return type;
        }

        return null;
    }
}
