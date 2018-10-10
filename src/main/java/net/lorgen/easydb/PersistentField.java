package net.lorgen.easydb;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;

public class PersistentField<T extends StoredItem> {

    private int fieldIndex;
    private Class<T> tClass;
    private Field field;
    private String name;
    private DataType type;
    private int size;
    private Class<?>[] typeParams;
    private Class<? extends FieldSerializer> serializerClass;
    private boolean key;
    private boolean autoIncr;
    private boolean index;
    private int indexId;

    public PersistentField(int fieldIndex, Class<T> tClass, Field field) {
        this.fieldIndex = fieldIndex;
        this.tClass = tClass;
        this.field = field;

        Persist annotation = field.getAnnotation(Persist.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Missing \"Persist\" annotation for field \"" + field.getName() + "\"!");
        }

        this.name = StringUtils.isEmpty(annotation.name()) ? field.getName() : annotation.name();

        this.serializerClass = annotation.serializer();
        this.type = annotation.type() == DataType.AUTO ? (this.hasCustomSerializer() ? DataType.CUSTOM : DataType.resolve(field)) : annotation.type();

        if (this.type == null) {
            throw new IllegalArgumentException("Unable to find data type for for field \"" + field.getName() + "\"!");
        }

        this.size = annotation.size();
        this.typeParams = annotation.typeParams();
        this.index = field.isAnnotationPresent(Index.class);
        if (this.index) {
            this.indexId = field.getAnnotation(Index.class).value();
        }

        StorageKey keyAnnot = field.getAnnotation(StorageKey.class);
        if (keyAnnot == null) {
            return; // Not a storage key
        }

        this.key = true;
        this.autoIncr = keyAnnot.autoIncrement();
    }

    public Object get(T object) {
        try {
            boolean accessible = this.field.isAccessible();
            this.field.setAccessible(true);

            Object val = this.field.get(object);

            // Restore to previous state
            this.field.setAccessible(accessible);

            return val;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FieldValue<T> getValue(T object) {
        Object value = this.get(object);
        if (value == null && !this.canBeNull()) {
            throw new NullPointerException("Field tagged as not-null \"" + this.field.getName() + "\" is null in given object!");
        }

        return new FieldValue<>(this, value);
    }

    public void set(StorageManager<T> manager, T object, String value) {
        this.set(object, this.getType().fromString(manager, this, value));
    }

    public void set(T object, Object value) {
        try {
            boolean accessible = this.field.isAccessible();
            this.field.setAccessible(true);

            this.field.set(object, value);

            // Restore to previous state
            this.field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public Class<T> getTypeClass() {
        return tClass;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public Class<?>[] getTypeParameters() {
        return typeParams;
    }

    public boolean isStorageKey() {
        return key;
    }

    public boolean isAutoIncrement() {
        return autoIncr;
    }

    public boolean isIndex() {
        return index;
    }

    public int getIndexId() {
        return indexId;
    }

    public boolean hasCustomSerializer() {
        return this.getSerializerClass() != DataType.class;
    }

    public Class<? extends FieldSerializer> getSerializerClass() {
        return serializerClass;
    }

    public boolean canBeNull() {
        return !this.isStorageKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PersistentField)) {
            return false;
        }

        PersistentField<?> that = (PersistentField<?>) o;

        return new EqualsBuilder()
          .append(tClass, that.tClass)
          .append(field, that.field)
          .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(tClass)
          .append(field)
          .toHashCode();
    }

    @Override
    public String toString() {
        return "PersistentField{" +
          "tClass=" + tClass +
          ", field=" + field +
          ", name='" + name + '\'' +
          ", type=" + type +
          ", size=" + size +
          ", typeParams=" + Arrays.toString(typeParams) +
          ", serializerClass=" + serializerClass +
          ", key=" + key +
          ", autoIncr=" + autoIncr +
          ", index=" + index +
          ", indexId=" + indexId +
          '}';
    }
}
