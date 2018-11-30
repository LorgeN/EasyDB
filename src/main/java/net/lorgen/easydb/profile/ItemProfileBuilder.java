package net.lorgen.easydb.profile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.field.FieldBuilder;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.interact.JoinWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows you to build a
 *
 * @param <T>
 */
public class ItemProfileBuilder<T> {

    private Class<T> typeClass;
    private PersistentField<T>[] fields;

    public ItemProfileBuilder(Class<T> typeClass) {
        this.typeClass = typeClass;
        this.fields = new PersistentField[0];
    }

    public ItemProfileBuilder<T> fromTypeClass() {
        int index = this.fields.length;
        for (Field field : this.typeClass.getDeclaredFields()) {
            // This one really doesn't need a lot of explanation
            // (Unless you don't know what transient is, in that case GOOGLE IT)
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            this.addField(new PersistentField<>(index++, typeClass, field));
        }

        return this;
    }

    public ProfileFieldBuilder<T> newField() {
        return new ProfileFieldBuilder<>(this, new FieldBuilder<>(this.fields.length, this.typeClass));
    }

    public ItemProfileBuilder<T> addField(PersistentField<T> field) {
        this.fields = Arrays.copyOf(this.fields, this.fields.length + 1);
        int expectedIndex = this.fields.length - 1;
        if (expectedIndex != field.getFieldIndex()) {
            throw new IllegalArgumentException("Invalid index!");
        }

        this.fields[expectedIndex] = field;
        return this;
    }

    public ItemProfile<T> build() {
        List<PersistentField<T>> fields = Lists.newArrayList(this.fields);

        PersistentField<T>[] storedFields = fields.stream()
          .filter(field -> !field.isExternalStore())
          .toArray(PersistentField[]::new);

        PersistentField<T>[] keys = Arrays.stream(storedFields)
          .filter(PersistentField::isStorageKey)
          .toArray(PersistentField[]::new);

        JoinWrapper[] joins = Arrays.stream(this.fields)
          .filter(PersistentField::isJoined)
          .map(JoinWrapper::new)
          .distinct()
          .toArray(JoinWrapper[]::new);

        if (keys.length == 0) {
            // You could store without a key, but then we'd have to do some special handling, at least in Redis. It
            // might be useful for just a simple lists where we only operate on the entire list but that is not very
            // scalable.
            throw new IllegalStateException("No storage keys! Please annotate fields using StorageKey to mark them as keys!");
        }

        PersistentField<T> autoIncrementField = Arrays.stream(this.fields)
          .filter(PersistentField::isAutoIncrement)
          .findFirst().orElse(null);

        PersistentField<T>[] indices = Arrays.stream(this.fields)
          .filter(PersistentField::isIndex)
          .toArray(PersistentField[]::new);
        if (indices.length == 0) {
            return new ItemProfile<>(this.typeClass, keys, this.fields, storedFields,
              autoIncrementField, new WrappedIndex[0], new WrappedIndex[0], joins);
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

        WrappedIndex[] indicesArray = indicesList.toArray(new WrappedIndex[0]);
        WrappedIndex[] uniqueIndices = Arrays.stream(indicesArray)
          .filter(WrappedIndex::isUnique)
          .toArray(WrappedIndex[]::new);

        return new ItemProfile<>(this.typeClass, keys, this.fields, storedFields,
          autoIncrementField, indicesArray, uniqueIndices, joins);
    }
}
