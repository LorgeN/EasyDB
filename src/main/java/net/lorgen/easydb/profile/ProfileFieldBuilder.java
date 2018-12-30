package net.lorgen.easydb.profile;

import net.lorgen.easydb.DataType;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Key;
import net.lorgen.easydb.Options;
import net.lorgen.easydb.field.FieldBuilder;
import net.lorgen.easydb.field.FieldSerializer;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.interact.external.External;
import net.lorgen.easydb.interact.join.Join;

import java.lang.reflect.Field;

/**
 * A delegated child of {@link FieldBuilder}, for special use in a
 * {@link ItemProfileBuilder}.
 *
 * @param <T> The type of the profile we are building
 */
public class ProfileFieldBuilder<T> {

    private ItemProfileBuilder<T> profileBuilder;
    private FieldBuilder<T> fieldBuilder;

    protected ProfileFieldBuilder(ItemProfileBuilder<T> profileBuilder, FieldBuilder<T> fieldBuilder) {
        this.profileBuilder = profileBuilder;
        this.fieldBuilder = fieldBuilder;
    }

    /**
     * Copies the key attributes (type class, name, size, type parameters, type,
     * serializer class) from the given {@link PersistentField field} to this
     * builder.
     *
     * @param field The {@link PersistentField field}
     * @return This
     */
    public ProfileFieldBuilder<T> copyAttributes(PersistentField<?> field) {
        fieldBuilder.copyAttributes(field);
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
    public ProfileFieldBuilder<T> setField(Field field, boolean autoConfig) {
        fieldBuilder.setField(field, autoConfig);
        return this;
    }

    /**
     * Takes the data from the field, ether from the {@link Options} annotation if
     * present, or based on assumptions, and sets the values in this builder
     *
     * @param field The {@link Field field}
     * @return This
     * @throws IllegalArgumentException If no data type for the field was found
     */
    public ProfileFieldBuilder<T> setData(Field field) {
        fieldBuilder.setData(field);
        return this;
    }

    /**
     * Sets the name of this field, used for defining e. g. column names etc.
     *
     * @param name The name
     * @return This
     */
    public ProfileFieldBuilder<T> setName(String name) {
        fieldBuilder.setName(name);
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
    public ProfileFieldBuilder<T> setType(DataType type) {
        fieldBuilder.setType(type);
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
    public ProfileFieldBuilder<T> setSerializerClass(Class<? extends FieldSerializer> serializerClass) {
        fieldBuilder.setSerializerClass(serializerClass);
        return this;
    }

    /**
     * The type class of this field. For normal fields in a class, this would be equal to
     * {@link Field#getType()}.
     *
     * @param typeClass The type of this field
     * @return This
     */
    public ProfileFieldBuilder<T> setTypeClass(Class<?> typeClass) {
        fieldBuilder.setTypeClass(typeClass);
        return this;
    }

    /**
     * Sets the size/length of this field. Only relevant for databases where this is a limiter,
     * e. g. SQL.
     *
     * @param size The size of this field
     * @return This
     */
    public ProfileFieldBuilder<T> setSize(int size) {
        fieldBuilder.setSize(size);
        return this;
    }

    /**
     * Sets the type parameters of the field, in chronological order.
     *
     * @param typeParams The type parameters, as {@link Class classes}
     * @return This
     */
    public ProfileFieldBuilder<T> setTypeParameters(Class<?>... typeParams) {
        fieldBuilder.setTypeParameters(typeParams);
        return this;
    }

    /**
     * Sets this field to be used as a {@link Key key}
     *
     * @param value If this field should be a {@link Key key}
     * @return This
     */
    public ProfileFieldBuilder<T> setAsKey(boolean value) {
        fieldBuilder.setAsKey(value);
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
    public ProfileFieldBuilder<T> setAutoIncrement(boolean value) {
        fieldBuilder.setAutoIncrement(value);
        return this;
    }

    /**
     * Sets the {@link Index index} data based on the annotations on the
     * given field.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public ProfileFieldBuilder<T> setIndex(Field field) {
        fieldBuilder.setIndex(field);
        return this;
    }

    /**
     * Sets this field as an index using default options from {@link Index}.
     * If no index IDs are set, these will automatically be set to -1.
     *
     * @param value If this field should be an index or not
     * @return This
     */
    public ProfileFieldBuilder<T> setIndex(boolean value) {
        fieldBuilder.setIndex(value);
        return this;
    }

    /**
     * Sets this field as a unique index. Also invokes {@link #setIndex(boolean)}.
     *
     * @return This
     */
    public ProfileFieldBuilder<T> setUniqueIndex() {
        fieldBuilder.setUniqueIndex();
        return this;
    }

    /**
     * Sets the index IDs, as in {@link Index#value()}.
     *
     * @param indexIds The index IDs
     * @return This
     */
    public ProfileFieldBuilder<T> setIndexIds(int... indexIds) {
        fieldBuilder.setIndexIds(indexIds);
        return this;
    }

    /**
     * Sets the {@link Join join} data based on the annotations of the given field.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public ProfileFieldBuilder<T> setJoinOn(Field field) {
        fieldBuilder.setJoinOn(field);
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
    public ProfileFieldBuilder<T> setJoinOn(String joinTable, String localField, String externalField, Class<? extends ItemRepository> repository) {
        fieldBuilder.setJoinOn(joinTable, localField, externalField, repository);
        return this;
    }

    /**
     * Sets the table this field should be fetched from, using the annotation
     * data from the {@link External} annotation on the given field if present.
     *
     * @param field The {@link Field field}
     * @return This
     */
    public ProfileFieldBuilder<T> setTable(Field field) {
        fieldBuilder.setTable(field);
        return this;
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param repository The {@link Class class} of the repository managing this field
     * @return This
     */
    public ProfileFieldBuilder<T> setTable(Class<? extends ItemRepository> repository) {
        fieldBuilder.setTable(repository);
        return this;
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param table The table to get from
     * @return This
     */
    public ProfileFieldBuilder<T> setTable(String table) {
        fieldBuilder.setTable(table);
        return this;
    }

    /**
     * Sets the table this field should be fetched from
     *
     * @param repository     The {@link Class class} of the repository managing this field
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
    public ProfileFieldBuilder<T> setTable(Class<? extends ItemRepository> repository, boolean saveKeyLocally, boolean immutable, String[] keyFields) {
        fieldBuilder.setTable(repository, saveKeyLocally, immutable, keyFields);
        return this;
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
    public ProfileFieldBuilder<T> setTable(String table, boolean saveKeyLocally, boolean immutable, String[] keyFields) {
        fieldBuilder.setTable(table, saveKeyLocally, immutable, keyFields);
        return this;
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
    public ProfileFieldBuilder<T> setTable(String table, boolean saveKeyLocally, boolean immutable, String[] keyFields, Class<? extends ItemRepository> repository) {
        fieldBuilder.setTable(table, saveKeyLocally, immutable, keyFields, repository);
        return this;
    }

    /**
     * Builds this field from the underlying {@link FieldBuilder builder}, and adds
     * it to the {@link ItemProfileBuilder profile}.
     *
     * @return The {@link ItemProfileBuilder profile builder}
     */
    public ItemProfileBuilder<T> buildAndAddField() {
        this.profileBuilder.addField(this.fieldBuilder.build());
        return this.profileBuilder;
    }
}
