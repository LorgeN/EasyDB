package net.lorgen.easydb;

import net.lorgen.easydb.interact.GetFromTable;
import net.lorgen.easydb.interact.Join;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

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
    private boolean uniqueIndex;
    private boolean externalStore;
    private String joinTable;
    private String joinLocalField;
    private String joinExternalField;
    private String tableStore;
    private boolean updateOnSave;
    private int[] indexIds;

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
            Index indexAnnot = field.getAnnotation(Index.class);
            this.uniqueIndex = indexAnnot.unique();
            this.indexIds = indexAnnot.value();
        }

        Join joinData = field.getAnnotation(Join.class);
        if (joinData != null) {
            this.externalStore = true;
            this.joinTable = joinData.table();
            this.joinLocalField = joinData.localField();
            this.joinExternalField = joinData.externalField();
        }

        GetFromTable table = field.getAnnotation(GetFromTable.class);
        if (table != null) {

        }

        StorageKey keyAnnot = field.getAnnotation(StorageKey.class);
        if (keyAnnot == null) {
            return; // Not a storage key
        }

        this.key = true;
        this.autoIncr = keyAnnot.autoIncrement();
    }

    public Object get(T object) {
        Validate.notNull(object);

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
        Validate.notNull(object);

        Object value = this.get(object);
        if (value == null && !this.canBeNull()) {
            throw new NullPointerException("Field tagged as not-null \"" + this.field.getName() + "\" is null in given object!");
        }

        return new FieldValue<>(this, value);
    }

    public void set(ItemRepository<T> manager, T object, String value) {
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

    public boolean isUniqueIndex() {
        return uniqueIndex;
    }

    public int[] getIndexIds() {
        return indexIds;
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

    public boolean isExternalStore() {
        return externalStore;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public String getJoinLocalField() {
        return joinLocalField;
    }

    public String getJoinExternalField() {
        return joinExternalField;
    }

    public String getTableStore() {
        return tableStore;
    }

    public boolean isUpdateOnSave() {
        return updateOnSave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersistentField<?> that = (PersistentField<?>) o;
        return Objects.equals(tClass, that.tClass) &&
          Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tClass, field);
    }

    @Override
    public String toString() {
        return "PersistentField{" +
          "fieldIndex=" + fieldIndex +
          ", tClass=" + tClass +
          ", field=" + field +
          ", name='" + name + '\'' +
          ", type=" + type +
          ", size=" + size +
          ", typeParams=" + Arrays.toString(typeParams) +
          ", serializerClass=" + serializerClass +
          ", key=" + key +
          ", autoIncr=" + autoIncr +
          ", index=" + index +
          ", uniqueIndex=" + uniqueIndex +
          ", externalStore=" + externalStore +
          ", joinTable='" + joinTable + '\'' +
          ", joinLocalField='" + joinLocalField + '\'' +
          ", joinExternalField='" + joinExternalField + '\'' +
          ", tableStore='" + tableStore + '\'' +
          ", updateOnSave=" + updateOnSave +
          ", indexIds=" + Arrays.toString(indexIds) +
          '}';
    }
}
