package net.lorgen.easydb;

import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.QueryBuilder;
import net.lorgen.easydb.util.Callback;
import net.lorgen.easydb.util.concurrency.UtilConcurrency;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemRepository<T extends StoredItem> {

    private String tableName;
    private Class<T> typeClass;
    private StoredItemProfile<T> profile;
    private DatabaseTypeAccessor<T> accessor;

    public ItemRepository(ConnectionConfiguration configuration, String tableName, Class<T> typeClass, DataAccessFrequency frequency) {
        this(configuration, tableName, typeClass, frequency.getRecommendedType());
    }

    public ItemRepository(ConnectionConfiguration configuration, String tableName, Class<T> typeClass, DatabaseType type) {
        this.tableName = tableName;
        this.typeClass = typeClass;
        this.profile = new StoredItemProfile<>(this.typeClass);
        this.accessor = type.newAccessor(configuration, this, this.tableName);
    }

    public ItemRepository(String tableName, Class<T> typeClass, DataAccessFrequency frequency) {
        this(tableName, typeClass, frequency.getRecommendedType());
    }

    public ItemRepository(String tableName, Class<T> typeClass, DatabaseType type) {
        this.tableName = tableName;
        this.typeClass = typeClass;
        this.profile = new StoredItemProfile<>(this.typeClass);
        this.accessor = type.newAccessor(this, this.tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    public DatabaseTypeAccessor<T> getDatabaseAccessor() {
        return accessor;
    }

    public StoredItemProfile<T> getProfile() {
        return profile;
    }

    public QueryBuilder<T> newQuery() {
        return new QueryBuilder<>(this);
    }

    public void findFirstAsync(Query<T> query, Callback<T> callback) {
        UtilConcurrency.submit(() -> {
            T value = this.findFirstSync(query);
            callback.call(value);
        });
    }

    public T findFirstSync(Query<T> query) {
        return accessor.findFirst(query);
    }

    public void findAllAsync(Query<T> query, Callback<List<T>> callback) {
        UtilConcurrency.submit(() -> {
            List<T> value = this.findAllSync(query);
            callback.call(value);
        });
    }

    public List<T> findAllSync(Query<T> query) {
        return accessor.findAll(query);
    }

    public void saveAsync(T object) {
        this.saveAsync(this.newQuery()
          .set(object)
          .build());
    }

    public void saveAsync(Query<T> query) {
        this.saveAsync(query, null);
    }

    public void saveAsync(Query<T> query, Runnable callback) {
        UtilConcurrency.submit(() -> {
            this.saveSync(query);
            if (callback == null) {
                return;
            }

            callback.run();
        });
    }

    public void saveSync(T object) {
        this.saveSync(this.newQuery()
          .set(object)
          .build());
    }

    public void saveSync(Query<T> query) {
        query.getObjectInstance().ifPresent(StoredItem::preSave);

        this.accessor.saveOrUpdate(query);

        query.getObjectInstance().ifPresent(StoredItem::postSave);
    }

    public void deleteAsync(T object) {
        this.deleteAsync(this.newQuery()
          .where()
          .andKeysAre(object)
          .closeAll()
          .build());
    }

    public void deleteAsync(Query<T> query) {
        this.deleteAsync(query, null);
    }

    public void deleteAsync(Query<T> query, Runnable callback) {
        UtilConcurrency.submit(() -> {
            this.deleteSync(query);
            if (callback == null) {
                return;
            }

            callback.run();
        });
    }

    public void deleteSync(T object) {
        this.deleteSync(this.newQuery()
          .where()
          .andKeysAre(object)
          .closeAll()
          .build());
    }

    public void deleteSync(Query<T> query) {
        query.getObjectInstance().ifPresent(StoredItem::preDelete);

        this.accessor.delete(query);

        query.getObjectInstance().ifPresent(StoredItem::postDelete);
    }

    public T fromValues(FieldValue<T>[] values) {
        try {
            Constructor<T>[] constructors = (Constructor<T>[]) this.getTypeClass().getDeclaredConstructors();
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
                  .map(name -> this.getProfile().resolveField(name))
                  .toArray(PersistentField[]::new);

                Object[] fieldValues = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    fieldValues[i] = this.getArrayValue(fields[i], values);
                }

                instance = annotated.newInstance(fieldValues);
            } else if (noArgs != null) {
                instance = noArgs.newInstance();
            } else {
                throw new RuntimeException("Missing constructor for StoredItem class " + this.getTypeClass().getSimpleName() + "!");
            }

            this.injectArrayValues(instance, values);
            return instance;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void injectArrayValues(T instance, FieldValue<T>[] values) {
        instance.preInject();

        for (FieldValue<T> value : values) {
            value.getField().set(instance, value.getValue());
        }

        instance.postInject();
    }

    public void updateArrayValue(PersistentField<T> field, Object value, FieldValue<T>[] values) {
        Arrays.stream(values)
          .filter(fieldValue -> fieldValue.getField().equals(field))
          .forEach(fieldValue -> fieldValue.setValue(value));
    }

    public Object getArrayValue(PersistentField<T> field, FieldValue<T>[] values) {
        return Arrays.stream(values)
          .filter(value -> value.getField().equals(field))
          .map(FieldValue::getValue)
          .findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemRepository<?> that = (ItemRepository<?>) o;
        return Objects.equals(tableName, that.tableName) &&
          Objects.equals(typeClass, that.typeClass) &&
          Objects.equals(profile, that.profile) &&
          Objects.equals(accessor, that.accessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, typeClass, profile, accessor);
    }

    @Override
    public String toString() {
        return "ItemRepository{" +
          "tableName='" + tableName + '\'' +
          ", typeClass=" + typeClass +
          ", profile=" + profile +
          ", accessor=" + accessor +
          '}';
    }
}
