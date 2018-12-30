package net.lorgen.easydb.field;

import net.lorgen.easydb.DataType;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Key;
import net.lorgen.easydb.Options;
import net.lorgen.easydb.interact.external.External;
import net.lorgen.easydb.interact.join.Join;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class PersistentField<T> {

    // TODO: Convert to wrappers?

    private int fieldIndex;
    private Class<T> tClass;
    private Field field;
    private Class<?> typeClass;
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
    private boolean transientSaving;
    private String[] keyFields;
    private Class<? extends ItemRepository> repository;
    private int[] indexIds;

    protected PersistentField(int fieldIndex, Class<T> tClass, Field field, String name, DataType type, int size,
                              Class<?>[] typeParams, Class<? extends FieldSerializer> serializerClass, boolean key,
                              boolean autoIncr, boolean index, boolean uniqueIndex, boolean externalStore,
                              String joinTable, String joinLocalField, String joinExternalField, String tableStore,
                              boolean transientSaving, String[] keyFields, Class<? extends ItemRepository> repository,
                              int[] indexIds, Class<?> typeClass) {
        this.fieldIndex = fieldIndex;
        this.tClass = tClass;
        this.field = field;
        this.name = name;
        this.type = type;
        this.size = size;
        this.typeParams = typeParams;
        this.serializerClass = serializerClass;
        this.key = key;
        this.autoIncr = autoIncr;
        this.index = index;
        this.uniqueIndex = uniqueIndex;
        this.externalStore = externalStore;
        this.joinTable = joinTable;
        this.joinLocalField = joinLocalField;
        this.joinExternalField = joinExternalField;
        this.tableStore = tableStore;
        this.transientSaving = transientSaving;
        this.keyFields = keyFields;
        this.repository = repository;
        this.indexIds = indexIds;
        this.typeClass = typeClass;
    }

    public PersistentField(int fieldIndex, Class<T> tClass, Field field) {
        this.fieldIndex = fieldIndex;
        this.tClass = tClass;
        this.field = field;
        this.typeClass = field.getType();

        Options annotation = field.getAnnotation(Options.class);
        if (annotation != null) {
            this.name = StringUtils.isEmpty(annotation.name()) ? field.getName() : annotation.name();
            this.serializerClass = annotation.serializer();
            this.type = annotation.type() == DataType.AUTO ? (this.serializerClass != DataType.class ? DataType.CUSTOM : DataType.resolve(field)) : annotation.type();
            this.size = annotation.size();
            this.typeParams = annotation.typeParams();
        } else {
            this.name = field.getName();
            this.serializerClass = DataType.class;
            this.type = DataType.resolve(field);
            this.size = 16;
            this.typeParams = new Class[0];
        }

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
            this.repository = joinData.repository();
        } else {
            External table = field.getAnnotation(External.class);
            if (table != null) {
                this.tableStore = table.table();
                this.externalStore = !table.saveKeyLocally();
                this.type = DataType.STRING;
                this.keyFields = table.keyFields();
                this.transientSaving = table.immutable();
                this.repository = table.repository();
            }
        }

        if (this.type == null) {
            throw new IllegalArgumentException("Unable to find data type for for field \"" + field.getName() + "\"!");
        }

        Key keyAnnot = field.getAnnotation(Key.class);
        if (keyAnnot == null) {
            return; // Not a storage key
        }

        this.key = true;
        this.autoIncr = keyAnnot.autoIncrement();
    }

    public Object getRawFieldValue(T object) {
        Validate.notNull(object);

        Field field;
        if (this.field != null) {
            field = this.field;
        } else {
            Class<T> objectClass = (Class<T>) object.getClass();

            try {
                field = objectClass.getDeclaredField(this.getName());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Object value = field.get(object);

            // Restore to previous state
            field.setAccessible(accessible);

            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FieldValue<T> getValue(T object) {
        Validate.notNull(object);

        Object value = this.getRawFieldValue(object);
        if (value == null && !this.canBeNull()) {
            throw new NullPointerException("Field tagged as not-null \"" + this.field.getName() + "\" is null in given object!");
        }

        return new FieldValue<>(this, value);
    }

    public void set(ItemRepository<T> manager, T object, String value) {
        this.set(object, this.getType().fromString(manager, this, value));
    }

    public void set(T object, Object value) {
        if (this.field == null) {
            return;
        }

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

    public Class<T> getDeclaringClass() {
        return tClass;
    }

    public Class<?> getTypeClass() {
        return typeClass;
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

    public void setType(DataType type) {
        this.type = type;
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

    public boolean isJoined() {
        return this.getJoinTable() != null;
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

    public String getExternalTable() {
        return tableStore;
    }

    public boolean isTransient() {
        return transientSaving;
    }

    public boolean isAutoIncr() {
        return autoIncr;
    }

    public String[] getKeyFields() {
        return keyFields;
    }

    public Class<? extends ItemRepository> getRepository() {
        return repository;
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
          Objects.equals(name, that.name);
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
          ", transientSaving=" + transientSaving +
          ", repository=" + repository +
          ", indexIds=" + Arrays.toString(indexIds) +
          '}';
    }
}
