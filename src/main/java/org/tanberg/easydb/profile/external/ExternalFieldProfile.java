package org.tanberg.easydb.profile.external;

import com.google.common.collect.Lists;
import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.ListenableTypeAccessor;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.interact.external.External;
import org.tanberg.easydb.interact.external.ExternalCollectionQueryHelper;
import org.tanberg.easydb.interact.external.ExternalFieldQueryHelper;
import org.tanberg.easydb.interact.external.ExternalMapQueryHelper;
import org.tanberg.easydb.interact.external.KeyRelation;
import org.tanberg.easydb.interact.external.KeyRelation.KeyHolder;
import org.tanberg.easydb.interact.external.QueryHelper;
import org.tanberg.easydb.profile.ItemProfile;
import org.tanberg.easydb.profile.ItemProfileBuilder;
import org.tanberg.easydb.profile.external.strategy.ProfilerContext;
import org.tanberg.easydb.profile.external.strategy.ProfilerStrategy;
import org.tanberg.easydb.query.QueryBuilder;
import org.tanberg.easydb.query.ValueHolder;
import org.tanberg.easydb.query.req.RequirementBuilder;
import org.tanberg.easydb.query.response.Response;
import org.tanberg.easydb.util.UtilLog;
import org.tanberg.easydb.util.reflection.UtilType;
import org.tanberg.easydb.util.reflection.UtilField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Utility class for creating, storing and handling data about an {@link External external}
 * field in a saved object.
 *
 * @param <T> The type of the object that declares this field
 */
public class ExternalFieldProfile<T> {

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    private ItemProfile profile;
    private ListenableTypeAccessor<T> accessor;
    private PersistentField<T> field;
    private ProfilerContext context;
    private ProfilerStrategy strategy;
    private Class<?> indexClass;
    private boolean primitiveIndex;
    private boolean primitiveValue;
    private List<KeyRelation> keyRelations;

    public ExternalFieldProfile(ListenableTypeAccessor<T> accessor, PersistentField<T> field, ProfilerContext context, ProfilerStrategy strategy) {
        this.accessor = accessor;
        this.field = field;
        this.context = context;
        this.strategy = strategy;
        this.keyRelations = Lists.newArrayList();
    }

    public QueryHelper getNewQueryHelper() {
        switch (this.getContext()) {
            case FIELD:
                return new ExternalFieldQueryHelper(this);
            case COLLECTION:
                return new ExternalCollectionQueryHelper(this);
            case MAP:
                return new ExternalMapQueryHelper(this);
            default:
                throw new IllegalArgumentException("Unknown context!");
        }
    }

    private Class<?> getTypeClass() {
        if (this.context == ProfilerContext.COLLECTION) {
            return this.field.getTypeParameters()[0];
        }

        if (this.context == ProfilerContext.MAP) {
            return this.field.getTypeParameters()[1];
        }

        return this.field.getTypeClass();
    }

    public PersistentField<T> getField() {
        return field;
    }

    public ProfilerContext getContext() {
        return context;
    }

    public ProfilerStrategy getStrategy() {
        return strategy;
    }

    public Class<?> getIndexClass() {
        return indexClass;
    }

    public boolean isPrimitiveIndex() {
        return primitiveIndex;
    }

    public boolean needsToProvideIndex() {
        return this.getStrategy() == ProfilerStrategy.DECLARING_KEYS_WITH_INDEX;
    }

    public void save(ItemRepository repository, ValueHolder<T> valueHolder, Object index, Object value) {
        QueryBuilder builder = repository.newQuery();

        switch (this.getStrategy()) {
            case DIRECT_USE:
                break;
            case DECLARING_KEYS:
                // All keys should be pulled from declaring class, i. e. index doesn't matter
                for (KeyRelation key : this.keyRelations) {
                    builder.set(key.getNameStoredClass(), valueHolder.getValue(key.getNameDeclaringClass()));
                }
                break;
            case DECLARING_KEYS_WITH_INDEX:
                for (KeyRelation key : this.keyRelations) {
                    switch (key.getHolder()) {
                        case ADDED_INDEX:
                            PersistentField field = this.profile.resolveField(key.getNameStoredClass());
                            builder.set(field, field.getValue(index));
                            break;
                        case DECLARING_VALUE:
                            builder.set(key.getNameStoredClass(), valueHolder.getValue(key.getNameDeclaringClass()));
                            break;
                        default:
                            throw new IllegalArgumentException("Unkown key holder " + key.getHolder().name() + "!");
                    }
                }
                break;
        }

        if (this.primitiveValue) {
            builder.set(VALUE_FIELD, value);
        } else {
            builder.set(value);
        }

        builder.save();
    }

    public Object extractValue(Response entity) {
        this.info("Extracting value from " + entity + "...");
        Object value = this.primitiveValue ? entity.getValue(VALUE_FIELD).getValue() : entity.getInstance();
        this.info("Value found as " + value + ".");
        return value;
    }

    public Object extractKey(Response entity) {
        Class<?> keyClass = this.getField().getTypeParameters()[0];
        if (UtilType.shouldBeStoredInSingleField(keyClass)) {
            return entity.getValue(KEY_FIELD).getValue();
        }

        try {
            Object key = keyClass.newInstance();

            for (Field field : keyClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                field.set(key, entity.getValue(field.getName()).getValue());
            }

            return key;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public QueryBuilder<?> newQuery(ItemRepository repository, ValueHolder<T> valueHolder) {
        this.info("Computing query from response " + valueHolder + "...");
        RequirementBuilder<?> builder = repository.newQuery().where();

        for (KeyRelation keyRelation : this.keyRelations) {
            Object value = valueHolder.getValue(keyRelation.getNameDeclaringClass()).getValue();
            builder.equals(keyRelation.getNameStoredClass(), value);
        }

        return builder.closeAll();
    }

    public ItemProfile getProfile() {
        if (this.profile != null) {
            return this.profile;
        }

        ItemProfileBuilder builder = null;
        switch (this.getStrategy()) {
            case DIRECT_USE:
                builder = this.newBuilder();
                break;
            case DECLARING_KEYS:
                builder = this.createDeclaringKeyProfile();
                break;
            case DECLARING_KEYS_WITH_INDEX:
                switch (this.getContext()) {
                    case COLLECTION:
                        builder = this.createWithIndex(Integer.class);
                        break;
                    case MAP:
                        Class<?> tClass = this.field.getTypeParameters()[0];
                        builder = this.createWithIndex(tClass);
                        break;
                }
                break;
            default:
                throw new IllegalStateException("Couldn't compute field profile!");
        }

        this.keyRelations = this.computeKeys(this.profile = this.fromTypeClass(builder).build());
        return this.profile;
    }

    // Internals

    private void info(String msg) {
        UtilLog.info(this.getClass().getSimpleName() + " - " + this.field.getName() + ": " + msg);
    }

    private List<KeyRelation> computeKeys(ItemProfile localProfile) {
        List<KeyRelation> list = Lists.newArrayList();

        ItemProfile declaringProfile = this.accessor.getProfile();
        PersistentField[] localKeys = localProfile.getKeys();

        // In this case, the user has specified the keys to use
        if (this.field.getKeyFields().length != 0) {
            // You can only specify the keys if we are using the DIRECT_USE strategy
            if (this.getStrategy() != ProfilerStrategy.DIRECT_USE) {
                throw new IllegalStateException("Weird setup found! Should not provide key fields in this case!");
            }

            String[] keys = this.field.getKeyFields();

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];

                if (declaringProfile.resolveField(key) == null) {
                    throw new IllegalArgumentException("Unable to resolve field \"" + key + "\" in " + declaringProfile.getTypeClass().getSimpleName() + "!");
                }

                PersistentField localKey = localKeys[i];

                // Pull keys from declaring value
                KeyRelation relation = new KeyRelation(KeyHolder.DECLARING_VALUE, key, localKey.getName());
                list.add(relation);
            }

            return list;
        }

        for (PersistentField key : localKeys) {
            // Ignore this field, this key is added and can not be linked to anything
            if (key.getName().equals(KEY_FIELD)) {
                continue;
            }

            PersistentField declaringField = declaringProfile.resolveField(key.getName());
            // Check if this key belongs to the declaring class
            if (declaringField == null) {
                // No added index class means that the keys have to come from the declaring class
                if (this.indexClass == null) {
                    throw new IllegalArgumentException("Unable to find correlating key to \"" + declaringField + "\" for " + this);
                }

                // Check if the field exists in the index class
                try {
                    Field field = this.indexClass.getDeclaredField(key.getName());
                    if (field == null) {
                        throw new IllegalArgumentException("Unable to find correlating key to \"" + declaringField + "\" for " + this);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                KeyRelation relation = new KeyRelation(KeyHolder.ADDED_INDEX, key.getName(), key.getName());
                list.add(relation);
                continue;
            }

            // Key exists in the declaring class
            KeyRelation relation = new KeyRelation(KeyHolder.DECLARING_VALUE, declaringField.getName(), key.getName());
            list.add(relation);
        }

        return list;
    }

    private ItemProfileBuilder fromTypeClass(ItemProfileBuilder builder) {
        Class<?> typeClass = this.getTypeClass();
        if (UtilType.shouldBeStoredInSingleField(typeClass)) {
            builder.newField()
              .setName(VALUE_FIELD)
              .setTypeClass(typeClass)
              .buildAndAddField();

            this.primitiveValue = true;
            return builder;
        }

        return builder.fromTypeClass();
    }

    private ItemProfileBuilder createWithIndex(Class<?> indexClass) {
        ItemProfileBuilder builder = this.createDeclaringKeyProfile();
        this.appendIndex(builder, indexClass);
        return builder;
    }

    private void appendIndex(ItemProfileBuilder<?> builder, Class<?> indexClass) {
        this.indexClass = indexClass;

        if (UtilType.shouldBeStoredInSingleField(indexClass)) {
            this.appendPrimitiveIndex(builder, indexClass);
            return;
        }

        this.appendObjectIndex(builder, indexClass);
    }

    private void appendObjectIndex(ItemProfileBuilder<?> builder, Class<?> indexClass) {
        ItemProfile<?> indexProfile = new ItemProfile<>(indexClass);

        for (PersistentField<?> indexField : indexProfile.getFields()) {
            if (UtilField.hasField(this.getTypeClass(), indexField.getName())) {
                throw new IllegalArgumentException("Duplicate field \"" + indexField.getName() + "\"!");
            }

            this.info("Inserting key field " + indexField + "...");
            // Hopefully no one uses max val integer as index ID
            builder.newField().copyAttributes(indexField)
              .setAsKey(true)
              .setIndex(true).setIndexIds(Integer.MAX_VALUE)
              .buildAndAddField();
        }

        this.primitiveIndex = false;
    }

    private void appendPrimitiveIndex(ItemProfileBuilder<?> builder, Class<?> indexClass) {
        builder.newField()
          .setName(KEY_FIELD)
          .setTypeClass(indexClass)
          .setAsKey(true)
          .buildAndAddField();

        this.primitiveIndex = true;
    }

    private ItemProfileBuilder createDeclaringKeyProfile() {
        ItemProfileBuilder builder = this.newBuilder();

        ItemProfile<T> declaringProfile = this.accessor.getProfile();
        for (PersistentField<T> key : declaringProfile.getKeys()) {
            if (UtilField.hasField(this.getTypeClass(), key.getName())) {
                throw new IllegalArgumentException("Duplicate field \"" + key.getName() + "\"!");
            }

            this.info("Inserting key " + key + "...");
            // Hopefully no one uses max val integer as index ID
            builder.newField().copyAttributes(key)
              .setAsKey(true)
              .setIndex(true).setIndexIds(Integer.MAX_VALUE)
              .buildAndAddField();
        }

        return builder;
    }

    private ItemProfileBuilder newBuilder() {
        return new ItemProfileBuilder<>(this.getTypeClass());
    }
}
