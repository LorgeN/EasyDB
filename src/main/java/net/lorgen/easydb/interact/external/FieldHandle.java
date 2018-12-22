package net.lorgen.easydb.interact.external;

import com.google.common.collect.Lists;
import net.lorgen.easydb.DataType;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Key;
import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.event.Listener;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.profile.ItemProfileBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class FieldHandle<T> implements Listener {

    protected static final String KEY_FIELD = "key";
    protected static final String VALUE_FIELD = "value";

    private final ListenableTypeAccessor<T> accessor;
    private final PersistentField<T> field;

    private List<String> keys; // A list of the keys we use to store the values
    private boolean useAddedKeys; // Means that the keys used to store are not a part of the object we are storing
    private ItemRepository<?> repository; // The repository we use

    public FieldHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        this.accessor = accessor;
        this.field = field;
    }

    protected boolean hasKeys(Class<?> typeClass) {
        for (Field declaredField : typeClass.getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(Key.class)) {
                continue;
            }

            return true;
        }

        return false;
    }

    protected <E> ItemProfile<E> buildWithLocalKeys(Class<E> typeClass) {
        return this.complete(typeClass, this.createWithLocalKeys(typeClass));
    }

    protected <E> ItemProfile<E> buildWithLocalKeys(Class<E> typeClass, Class<?> additionalKey) {
        ItemProfileBuilder<E> profileBuilder = this.createWithLocalKeys(typeClass);

        // This should be the most common case
        if (DataType.resolve(additionalKey) != null) {
            profileBuilder.newField()
              .setName(KEY_FIELD)
              .setTypeClass(additionalKey)
              .setAsKey(true)
              .buildAndAddField();
        } else {
            for (Field field : additionalKey.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue; // Well, this would be interesting now wouldn't it
                }

                profileBuilder.newField()
                  .setField(field, true)
                  .setAsKey(true)
                  .buildAndAddField();
            }
        }

        return this.complete(typeClass, profileBuilder);
    }

    protected <E> ItemProfileBuilder<E> createWithLocalKeys(Class<E> typeClass) {
        ItemProfileBuilder<E> profileBuilder = new ItemProfileBuilder<>(typeClass);

        for (PersistentField<T> key : this.accessor.getProfile().getKeys()) {
            profileBuilder.newField().copyAttributes(key).setAsKey(true).buildAndAddField();
        }

        return profileBuilder;
    }

    protected <E> ItemProfile<E> complete(Class<E> typeClass, ItemProfileBuilder<E> profileBuilder) {
        if (DataType.resolve(typeClass) != null) {
            profileBuilder.newField()
              .setName(VALUE_FIELD)
              .setTypeClass(this.field.getTypeClass())
              .buildAndAddField();
        } else {
            profileBuilder.fromTypeClass();
        }

        return profileBuilder.build();
    }

    protected void setToLocalKeys() {
        List<String> list = Lists.newArrayList();

        for (PersistentField<T> key : this.getAccessor().getProfile().getKeys()) {
            list.add(key.getName());
        }

        this.setKeys(list);
    }

    protected void computeKeys() {
        if (this.getField().getKeyFields() != null && this.getField().getKeyFields().length != 0) {
            List<String> keys = Lists.newArrayList();

            for (String keyFieldName : this.getField().getKeyFields()) {
                PersistentField<T> keyField = this.getAccessor().getProfile().resolveField(keyFieldName);
                if (keyField == null) {
                    throw new IllegalArgumentException("Unrecognised field \"" + keyFieldName + "\" in profile " + this.getAccessor().getProfile());
                }

                keys.add(keyField.getName());
            }

            this.setKeys(keys);
            return;
        }

        List<String> equalFields = Lists.newArrayList();

        ItemProfile<T> originalProfile = this.getAccessor().getProfile();
        ItemProfile<?> localProfile = this.getRepository().getProfile();

        for (PersistentField<T> storedField : originalProfile.getStoredFields()) {
            if (!this.getAccessor().isSearchable(storedField)) {
                continue;
            }

            PersistentField similarField = localProfile.resolveField(storedField.getName());
            if (similarField == null || !this.getRepository().isSearchable(similarField)) {
                continue;
            }

            if (similarField.getType() != storedField.getType()) {
                continue;
            }

            equalFields.add(storedField.getName());
        }

        if (equalFields.isEmpty()) {
            throw new IllegalArgumentException("No common fields found!");
        }

        this.setKeys(equalFields);
    }

    protected boolean isPrimitive() {
        return this.isPrimitive(this.getField().getTypeClass());
    }

    protected boolean isPrimitive(Class<?> typeClass) {
        return DataType.resolve(typeClass) != null;
    }

    // Getters and setters

    public ListenableTypeAccessor<T> getAccessor() {
        return accessor;
    }

    public PersistentField<T> getField() {
        return field;
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.fromAccessor(this.getAccessor());
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public boolean useAddedKeys() {
        return useAddedKeys;
    }

    public void setUseAddedKeys(boolean useAddedKeys) {
        this.useAddedKeys = useAddedKeys;
    }

    public ItemRepository<?> getRepository() {
        return repository;
    }

    public void setRepository(ItemRepository<?> repository) {
        this.repository = repository;
    }
}
