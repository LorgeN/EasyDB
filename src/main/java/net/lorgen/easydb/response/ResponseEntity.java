package net.lorgen.easydb.response;

import net.lorgen.easydb.DeserializerConstructor;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * A response to a query upon a repository
 *
 * @param <T> The type of the repository
 */
public class ResponseEntity<T> {

    private ItemProfile<T> profile; // The profile of the item
    private FieldValue<T>[] values; // The loaded values
    // Use an optional here so that we know if we've tried to create it before,
    // and in that case don't try again, wasting resources
    private Optional<T> createdInstance; // The created instance (If created)

    public ResponseEntity(ItemProfile<T> profile) {
        this.profile = profile;
    }

    public ResponseEntity(ItemProfile<T> profile, FieldValue<T>[] values) {
        this.profile = profile;
        this.values = values;
    }

    public FieldValue<T>[] getValues() {
        return values;
    }

    public FieldValue<T> getValue(PersistentField<T> field) {
        return Arrays.stream(this.getValues())
          .filter(value -> value.getField().equals(field))
          .findFirst().orElse(null);
    }

    public FieldValue<T> getValue(String fieldName) {
        return Arrays.stream(this.getValues())
          .filter(value -> value.getField().getName().equalsIgnoreCase(fieldName))
          .findFirst().orElse(null);
    }

    public boolean isEmpty() {
        return this.values == null;
    }

    public T getInstance() {
        if (this.createdInstance != null) {
            return createdInstance.orElse(null);
        }

        try {
            Constructor<T>[] constructors = (Constructor<T>[]) this.profile.getTypeClass().getDeclaredConstructors();
            Constructor<T> noArgs = null;
            Constructor<T> annotated = null;

            for (Constructor<T> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    noArgs = constructor;
                    continue;
                }

                if (!constructor.isAnnotationPresent(DeserializerConstructor.class)) {
                    continue;
                }

                annotated = constructor;
            }

            T instance;
            if (annotated != null) {
                PersistentField<T>[] fields = Arrays.stream(annotated.getAnnotation(DeserializerConstructor.class).value())
                  .map(name -> this.profile.resolveField(name))
                  .toArray(PersistentField[]::new);

                Object[] fieldValues = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    fieldValues[i] = this.getArrayValue(fields[i], values);
                }

                instance = annotated.newInstance(fieldValues);
            } else if (noArgs != null) {
                instance = noArgs.newInstance();
            } else {
                throw new RuntimeException("Missing constructor for StoredItem class " + this.profile.getTypeClass().getSimpleName() + "!");
            }

            this.injectArrayValues(instance, values);

            this.createdInstance = Optional.of(instance);

            return instance;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            this.createdInstance = Optional.empty();
            return null;
        }
    }

    // Internals

    private void injectArrayValues(T instance, FieldValue<T>[] values) {
        if (instance instanceof StoredItem) {
            ((StoredItem) instance).preInject();
        }

        for (FieldValue<T> value : values) {
            value.getField().set(instance, value.getValue());
        }

        if (instance instanceof StoredItem) {
            ((StoredItem) instance).postInject();
        }
    }

    private Object getArrayValue(PersistentField<T> field, FieldValue<T>[] values) {
        return Arrays.stream(values)
          .filter(value -> value.getField().equals(field))
          .map(FieldValue::getValue)
          .findFirst().orElse(null);
    }
}
