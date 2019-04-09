package org.tanberg.easydb.profile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.interact.join.JoinWrapper;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemProfile<T> {

    private Class<T> typeClass;
    private PersistentField<T>[] keys;
    private PersistentField<T>[] fields;
    private PersistentField<T>[] storedFields;
    private PersistentField<T> autoIncrementField;
    private WrappedIndex<T>[] indices;
    private WrappedIndex<T>[] uniqueIndices;
    private JoinWrapper[] joins;

    public ItemProfile(Class<T> typeClass, PersistentField<T>[] keys, PersistentField<T>[] fields,
                       PersistentField<T>[] storedFields, PersistentField<T> autoIncrementField,
                       WrappedIndex<T>[] indices, WrappedIndex<T>[] uniqueIndices, JoinWrapper[] joins) {
        Validate.notNull(typeClass);
        Validate.notNull(keys);
        Validate.notNull(fields);
        Validate.notNull(storedFields);
        Validate.notNull(indices);
        Validate.notNull(uniqueIndices);
        Validate.notNull(joins);

        this.typeClass = typeClass;
        this.keys = keys;
        this.fields = fields;
        this.storedFields = storedFields;
        this.autoIncrementField = autoIncrementField;
        this.indices = indices;
        this.uniqueIndices = uniqueIndices;
        this.joins = joins;
    }

    public ItemProfile(Class<T> typeClass) {
        this.typeClass = typeClass;

        List<PersistentField<T>> fields = Lists.newArrayList();

        int index = 0;
        for (Field field : typeClass.getDeclaredFields()) {
            // This one really doesn't need a lot of explanation
            // (Unless you don't know what transient is, in that case GOOGLE IT)
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            fields.add(new PersistentField<>(index++, typeClass, field));
        }

        this.fields = fields.toArray(new PersistentField[0]);

        this.storedFields = fields.stream()
          .filter(field -> !field.isExternalStore())
          .toArray(PersistentField[]::new);

        this.keys = Arrays.stream(this.storedFields)
          .filter(PersistentField::isStorageKey)
          .toArray(PersistentField[]::new);

        this.joins = Arrays.stream(this.fields)
          .filter(PersistentField::isJoined)
          .map(JoinWrapper::new)
          .distinct()
          .toArray(JoinWrapper[]::new);

        if (this.keys.length == 0) {
            // You could store without a key, but then we'd have to do some special handling, at least in Redis. It
            // might be useful for just a simple lists where we only operate on the entire list but that is not very
            // scalable.
            throw new IllegalArgumentException("No storage keys! Please annotate fields using StorageKey to mark them as keys!");
        }

        this.fields = fields.stream()
          .toArray(PersistentField[]::new);

        this.autoIncrementField = Arrays.stream(this.getFields())
          .filter(PersistentField::isAutoIncrement)
          .findFirst().orElse(null);

        PersistentField<T>[] indices = Arrays.stream(this.getFields())
          .filter(PersistentField::isIndex)
          .toArray(PersistentField[]::new);
        if (indices.length == 0) {
            this.indices = new WrappedIndex[0];
            this.uniqueIndices = new WrappedIndex[0];
            return;
        }

        List<PersistentField<T>> soloIndices = Lists.newArrayList();
        Map<Integer, List<PersistentField<T>>> indexMap = Maps.newHashMap();

        for (PersistentField<T> currentIndex : indices) {
            for (int id : currentIndex.getIndexIds()) {
                if (id != -1) {
                    indexMap.computeIfAbsent(id, someId -> Lists.newArrayList()).add(currentIndex);
                    continue;
                }

                soloIndices.add(currentIndex);
            }
        }

        List<WrappedIndex<T>> indicesList = Lists.newArrayList();
        AtomicInteger id = new AtomicInteger(1);

        indexMap.forEach((integer, persistentFields) -> indicesList.add(new WrappedIndex<T>(
          id.getAndIncrement(),
          persistentFields.stream().allMatch(PersistentField::isUniqueIndex),
          persistentFields.toArray(new PersistentField[0])
        )));

        for (PersistentField<T> soloIndex : soloIndices) {
            indicesList.add(new WrappedIndex<>(id.getAndIncrement(), soloIndex.isUniqueIndex(), soloIndex));
        }

        this.indices = indicesList.toArray(new WrappedIndex[0]);
        this.uniqueIndices = Arrays.stream(this.getIndices())
          .filter(WrappedIndex::isUnique)
          .toArray(WrappedIndex[]::new);
    }

    public PersistentField<T> resolveField(String name) {
        return Arrays.stream(this.fields)
          .filter(field -> field.getName().equals(name) || (field.getField() != null && field.getField().getName().equals(name))) // Case-sensitive
          .findFirst().orElse(null);
    }

    public PersistentField<T> resolveKey(String name) {
        return Arrays.stream(this.keys)
          .filter(field -> field.getName().equals(name)) // Case-sensitive
          .findFirst().orElse(null);
    }

    public boolean isKey(PersistentField<T> field) {
        return Arrays.stream(this.getKeys()).anyMatch(key -> key.equals(field));
    }

    public boolean areKeys(PersistentField<T>... fields) {
        return fields.length == this.keys.length && Arrays.stream(fields).allMatch(this::isKey);
    }

    public WrappedIndex<T> getIndex(PersistentField<T>... fields) {
        for (WrappedIndex<T> index : this.getIndices()) {
            if (!index.areFields(fields)) {
                continue;
            }

            return index;
        }

        return null;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    public PersistentField<T>[] getStoredFields() {
        return storedFields;
    }

    public PersistentField<T>[] getKeys() {
        return keys;
    }

    public PersistentField<T>[] getFields() {
        return fields;
    }

    public PersistentField<T> getAutoIncrementField() {
        return autoIncrementField;
    }

    public WrappedIndex<T>[] getIndices() {
        return indices;
    }

    public WrappedIndex<T>[] getUniqueIndices() {
        return uniqueIndices;
    }

    public JoinWrapper[] getJoins() {
        return joins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ItemProfile)) {
            return false;
        }

        ItemProfile<?> that = (ItemProfile<?>) o;
        return Objects.equals(typeClass, that.typeClass) &&
          Arrays.equals(keys, that.keys) &&
          Arrays.equals(fields, that.fields) &&
          Arrays.equals(storedFields, that.storedFields) &&
          Objects.equals(autoIncrementField, that.autoIncrementField) &&
          Arrays.equals(indices, that.indices) &&
          Arrays.equals(uniqueIndices, that.uniqueIndices) &&
          Arrays.equals(joins, that.joins);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeClass, autoIncrementField);
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + Arrays.hashCode(fields);
        result = 31 * result + Arrays.hashCode(storedFields);
        result = 31 * result + Arrays.hashCode(indices);
        result = 31 * result + Arrays.hashCode(uniqueIndices);
        result = 31 * result + Arrays.hashCode(joins);
        return result;
    }

    @Override
    public String toString() {
        return "StoredItemProfile{" +
          "typeClass=" + typeClass +
          ", keys=" + Arrays.toString(keys) +
          ", fields=" + Arrays.toString(fields) +
          ", storedFields=" + Arrays.toString(storedFields) +
          ", autoIncrementField=" + autoIncrementField +
          ", indices=" + Arrays.toString(indices) +
          ", uniqueIndices=" + Arrays.toString(uniqueIndices) +
          ", joins=" + Arrays.toString(joins) +
          '}';
    }
}
