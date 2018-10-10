package net.lorgen.easydb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StoredItemProfile<T extends StoredItem> {

    private Class<T> typeClass;
    private PersistentField<T>[] keys;
    private PersistentField<T>[] fields;
    private PersistentField<T> autoIncrementField;
    private WrappedIndex<T>[] indices;

    public StoredItemProfile(Class<T> typeClass) {
        this.typeClass = typeClass;

        List<PersistentField<T>> fields = Lists.newArrayList();

        int index = 0;
        for (Field field : typeClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Persist.class)) {
                continue;
            }

            fields.add(new PersistentField<>(index++, typeClass, field));
        }

        this.keys = fields.stream()
          .filter(PersistentField::isStorageKey)
          .toArray(PersistentField[]::new);

        if (this.keys.length == 0) {
            // You could store without a key, but then we'd have to do some special handling, at least in Redis. It
            // might be useful for just a simple lists where we only operate on the entire list but that is not very
            // scalable.
            throw new IllegalStateException("No storage keys! Please annotate fields using StorageKey to mark them as keys!");
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
            return;
        }

        List<WrappedIndex<T>> indicesList = Lists.newArrayList();
        Map<Integer, List<PersistentField<T>>> indexMap = Maps.newHashMap();

        for (PersistentField<T> currentIndex : indices) {
            if (currentIndex.getIndexId() != -1) {
                indexMap.computeIfAbsent(currentIndex.getIndexId(), id -> Lists.newArrayList()).add(currentIndex);
                continue;
            }

            indicesList.add(new WrappedIndex<>(currentIndex));
        }

        indexMap.forEach((integer, persistentFields) -> indicesList.add(new WrappedIndex<T>(persistentFields.toArray(new PersistentField[0]))));
        this.indices = indicesList.toArray(new WrappedIndex[0]);
    }

    public PersistentField<T> resolveField(String name) {
        return Arrays.stream(this.fields)
          .filter(field -> field.getName().equals(name)) // Case-sensitive
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
        return Arrays.stream(fields).allMatch(this::isKey);
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
}
