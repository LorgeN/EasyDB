package net.lorgen.easydb.field;

import net.lorgen.easydb.DataType;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Persist;
import net.lorgen.easydb.StorageKey;
import net.lorgen.easydb.interact.external.External;
import net.lorgen.easydb.interact.join.Join;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;

/**
 * Builder for a {@link PersistentField}. Useful when you want to simply make a "field"
 * that doesn't actually exist in any class, e. g. for keys in a map, where you store the
 * object value in the map.
 *
 * @param <T> The type class of the repository
 */
public class FieldBuilder<T> {

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
    private boolean updateOnSave;
    private String[] keyFields;
    private Class<? extends ItemRepository> repository;
    private int[] indexIds;

    /**
     * Constructor
     *
     * @param fieldIndex The index of this field
     * @param tClass     The class of type parameter T
     */
    public FieldBuilder(int fieldIndex, Class<T> tClass) {
        this.fieldIndex = fieldIndex;
        this.tClass = tClass;
    }

    /**
     * Copies the key attributes (type class, name, size, type parameters, type,
     * serializer class) from the given {@link PersistentField field} to this
     * builder.
     *
     * @param field The {@link PersistentField field}
     * @return This
     */
    public FieldBuilder<T> copyAttributes(PersistentField<?> field) {
        this.typeClass = field.getTypeClass();
        this.name = field.getName();
        this.size = field.getSize();
        this.typeParams = field.getTypeParameters();
        this.type = field.getType();
        this.serializerClass = field.getSerializerClass();
        return this;
    }

    /**
     * Sets the {@link Field field} of this {@link PersistentField}.
     *
     * @param field      The {@link Field field}
     * @param autoConfig If the field's annotation and other data should be used
     *                   to automatically configure this builder data
     * @return This
     */
    public FieldBuilder<T> setField(Field field, boolean autoConfig) {
        Validate.isTrue(field.getDeclaringClass().equals(this.tClass), "Field not in type class!");

        this.field = field;
        this.typeClass = field.getType();

        if (!autoConfig) {
            return this;
        }

        this.setData(field);
        this.setIndex(field);
        this.setJoinOn(field);
        this.setTable(field);

        StorageKey keyAnnot = field.getAnnotation(StorageKey.class);
        if (keyAnnot == null) {
            return this; // Not a storage key
        }

        this.key = true;
        this.autoIncr = keyAnnot.autoIncrement();
        return this;
    }

    /**
     * Takes the data from the field, ether from the {@link Persist} annotation if
     * present, or based on assumptions, and sets the values in this builder
     *
     * @param field The {@link Field field}
     * @return This
     * @throws IllegalArgumentException If no data type for the field was found
     */
    public FieldBuilder<T> setData(Field field) {
        Persist annotation = field.getAnnotation(Persist.class);
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

        if (this.type == null) {
            throw new IllegalArgumentException("Unable to find data type for for field \"" + field.getName() + "\"!");
        }

        return this;
    }

    /**
     * Sets the name of this field, used for defining e. g. column names etc.
     *
     * @param name The name
     * @return This
     */
    public FieldBuilder<T> setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the {@link DataType data type} of this field. If set to {@link DataType#AUTO} calling
     * this method will automatically determine the best data type. Additionally, calling this method
     * will set {@link DataType} as the serializer class.
     * <p>
     * If you want to set the field to use {@link DataType#CUSTOM custom serializer} use the method
     * {@link #setSerializerClass(Class)} instead.
     *
     * @param type The {@link DataType type}
     * @return This
     * @throws IllegalArgumentException If type is {@link DataType#CUSTOM}
     */
    public FieldBuilder<T> setType(DataType type) {
        if (type == DataType.CUSTOM) {
            throw new IllegalArgumentException("Use method #setSerializerClass(Class) instead!");
        }

        this.serializerClass = DataType.class;
        this.type = type;
        return this;
    }

    /**
     * Sets the {@link FieldSerializer serializer} class of this field. If not set to {@link DataType},
     * calling this method will automatically update the {@link DataType data type} of this feld to
     * {@link DataType#CUSTOM}.
     *
     * @param serializerClass The serializer class
     * @return This
     */
    public FieldBuilder<T> setSerializerClass(Class<? extends FieldSerializer> serializerClass) {
        this.serializerClass = serializerClass;
        if (this.serializerClass != DataType.class) {
            this.type = DataType.CUSTOM;
        }

        return this;
    }

    /**
     * The type class of this field. For normal fields in a class, this would be equal to
     * {@link Field#getType()}.
     *
     * @param typeClass The type of this field
     * @return This
     */
    public FieldBuilder<T> setTypeClass(Class<?> typeClass) {
        this.typeClass = typeClass;
        return this;
    }

    /**
     * Sets the size/length of this field. Only relevant for databases where this is a limiter,
     * e. g. SQL.
     *
     * @param size The size of this field
     * @return This
     */
    public FieldBuilder<T> setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Sets the type parameters of the field, in chronological order.
     *
     * @param typeParams The type parameters, as {@link Class classes}
     * @return This
     */
    public FieldBuilder<T> setTypeParameters(Class<?>... typeParams) {
        this.typeParams = typeParams;
        return this;
    }

    /**
     * Sets this field to be used as a {@link StorageKey key}
     *
     * @param value If this field should be a {@link StorageKey key}
     * @return This
     */
    public FieldBuilder<T> setAsKey(boolean value) {
        this.key = value;
        return this;
    }

    /**
     * Sets this field to be automatically incremented.
     *
     * <b>Every repository can at max have one auto incremented field!</b>
     *
     * @param value If this field should auto increment
     * @return This
     */
    public FieldBuilder<T> setAutoIncrement(boolean value) {
        this.autoIncr = value;
        return this;
    }

    /**
     * Sets the {@link Index index} data based on the annotations on the
     * given field.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public FieldBuilder<T> setIndex(Field field) {
        Index indexAnnot = field.getAnnotation(Index.class);
        this.index = indexAnnot != null;
        if (!this.index) {
            return this;
        }

        this.uniqueIndex = indexAnnot.unique();
        this.indexIds = indexAnnot.value();
        return this;
    }

    /**
     * Sets this field as an index using default options from {@link Index}.
     * If no index IDs are set, these will automatically be set to -1.
     *
     * @param value If this field should be an index or not
     * @return This
     */
    public FieldBuilder<T> setIndex(boolean value) {
        this.index = value;
        if (!value) {
            return this;
        }

        if (this.indexIds.length == 0) {
            return this.setIndexIds(-1);
        }

        return this;
    }

    /**
     * Sets this field as a unique index. Also invokes {@link #setIndex(boolean)}.
     *
     * @return This
     */
    public FieldBuilder<T> setUniqueIndex() {
        this.uniqueIndex = true;
        return this.setIndex(true);
    }

    /**
     * Sets the index IDs, as in {@link Index#value()}.
     *
     * @param indexIds The index IDs
     * @return This
     */
    public FieldBuilder<T> setIndexIds(int... indexIds) {
        this.indexIds = indexIds;
        return this;
    }

    /**
     * Sets the {@link Join join} data based on the annotations of the given field.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public FieldBuilder<T> setJoinOn(Field field) {
        Join joinData = field.getAnnotation(Join.class);
        if (joinData == null) {
            return this;
        }

        this.externalStore = true;
        this.joinTable = joinData.table();
        this.joinLocalField = joinData.localField();
        this.joinExternalField = joinData.externalField();
        this.repository = joinData.repository();
        return this;
    }

    /**
     * Sets the {@link Join join} data
     *
     * @param joinTable     The table to join on
     * @param localField    The local field to match on
     * @param externalField The external field to match on
     * @param repository    The {@link ItemRepository repository} to get from
     * @return This
     */
    public FieldBuilder<T> setJoinOn(String joinTable, String localField, String externalField, Class<? extends ItemRepository> repository) {
        this.externalStore = true;
        this.joinTable = joinTable;
        this.joinLocalField = localField;
        this.joinExternalField = externalField;
        this.repository = repository;
        return this;
    }

    /**
     * Sets the table this field should be fetched from, using the annotation
     * data from the {@link External} annotation on the given field if present.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public FieldBuilder<T> setTable(Field field) {
        External table = field.getAnnotation(External.class);
        if (table == null) {
            return this;
        }

        this.tableStore = table.table();
        this.externalStore = !table.saveKeyLocally();
        this.keyFields = table.keyFields();
        this.updateOnSave = !table.immutable();
        this.repository = table.repository();
        return this;
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param repository The {@link Class class} of the repository managing this field
     * @return This
     */
    public FieldBuilder<T> setTable(Class<? extends ItemRepository> repository) {
        return this.setTable("", true, true, new String[0], repository);
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param table The table to get from
     * @return This
     */
    public FieldBuilder<T> setTable(String table) {
        return this.setTable(table, true, true, new String[0], ItemRepository.class);
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param saveKeyLocally If the key should be saved locally, as in if we
     *                       should take the key(s) from the object in this field
     *                       and store them in this repository
     * @param immutable      If this field is immutable, meaning that we shouldn't
     *                       save it when the parent object is saved
     * @param keyFields      The local field(s) to use as keys when finding external
     *                       object(s). If this is used, {@link External#saveKeyLocally()}
     *                       should be set to false. If {@link External#saveKeyLocally()}
     *                       isn't used, and this is left empty, an attempt will be made
     *                       to match keys in this type to that of the stored type in this
     *                       field.
     * @param repository     The {@link Class class} of the repository managing this field
     * @return This
     */
    public FieldBuilder<T> setTable(Class<? extends ItemRepository> repository, boolean saveKeyLocally, boolean immutable, String[] keyFields) {
        return this.setTable("", saveKeyLocally, immutable, keyFields, repository);
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param table          The table to get from
     * @param saveKeyLocally If the key should be saved locally, as in if we
     *                       should take the key(s) from the object in this field
     *                       and store them in this repository
     * @param immutable      If this field is immutable, meaning that we shouldn't
     *                       save it when the parent object is saved
     * @param keyFields      The local field(s) to use as keys when finding external
     *                       object(s). If this is used, {@link External#saveKeyLocally()}
     *                       should be set to false. If {@link External#saveKeyLocally()}
     *                       isn't used, and this is left empty, an attempt will be made
     *                       to match keys in this type to that of the stored type in this
     *                       field.
     * @return This
     */
    public FieldBuilder<T> setTable(String table, boolean saveKeyLocally, boolean immutable, String[] keyFields) {
        return this.setTable(table, saveKeyLocally, immutable, keyFields, ItemRepository.class);
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param table          The table to get from
     * @param saveKeyLocally If the key should be saved locally, as in if we
     *                       should take the key(s) from the object in this field
     *                       and store them in this repository
     * @param immutable      If this field is immutable, meaning that we shouldn't
     *                       save it when the parent object is saved
     * @param keyFields      The local field(s) to use as keys when finding external
     *                       object(s). If this is used, {@link External#saveKeyLocally()}
     *                       should be set to false. If {@link External#saveKeyLocally()}
     *                       isn't used, and this is left empty, an attempt will be made
     *                       to match keys in this type to that of the stored type in this
     *                       field.
     * @param repository     The {@link Class class} of the repository managing this field
     * @return This
     */
    public FieldBuilder<T> setTable(String table, boolean saveKeyLocally, boolean immutable, String[] keyFields, Class<? extends ItemRepository> repository) {
        this.tableStore = table;
        this.externalStore = !saveKeyLocally;
        this.updateOnSave = !immutable;
        this.keyFields = keyFields;
        this.repository = repository;
        return this;
    }

    /**
     * Builds this builders data into a {@link PersistentField field}
     *
     * @return The built {@link PersistentField field}
     */
    public PersistentField<T> build() {
        if (this.type == null || this.type == DataType.AUTO) {
            if (this.typeClass == null) {
                throw new IllegalArgumentException("Type class not defined! Can not use AUTO data type!");
            }

            this.type = DataType.resolve(this.typeClass);
        }

        return new PersistentField<>(fieldIndex, tClass, field, name, type, size, typeParams, serializerClass, key,
          autoIncr, index, uniqueIndex, externalStore, joinTable, joinLocalField, joinExternalField, tableStore,
          updateOnSave, keyFields, repository, indexIds, typeClass);
    }
}
