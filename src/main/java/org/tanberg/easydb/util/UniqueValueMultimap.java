package org.tanberg.easydb.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

public class UniqueValueMultimap<K, V> implements Multimap<K, V> {

    private final Multimap<K, V> keyToValues;
    private final Map<V, K> valueToKey;

    public UniqueValueMultimap() {
        this.keyToValues = ArrayListMultimap.create();
        this.valueToKey = Maps.newConcurrentMap();
    }

    public K getKey(V value) {
        return this.valueToKey.get(value);
    }

    public void removeValue(V value) {
        this.removeValue(value);
    }

    @Override
    public int size() {
        return keyToValues.size();
    }

    @Override
    public boolean isEmpty() {
        return keyToValues.isEmpty();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return keyToValues.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return keyToValues.containsValue(value);
    }

    @Override
    public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
        return keyToValues.containsEntry(key, value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean put(@Nullable K key, @Nullable V value) {
        this.valueToKey.put(value, key);
        return keyToValues.put(key, value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        this.valueToKey.remove(value);
        return keyToValues.remove(key, value);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
        for (V value : values) {
            this.valueToKey.put(value, key);
        }

        return keyToValues.putAll(key, values);
    }

    @Override
    @CanIgnoreReturnValue
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        for (Entry<? extends K, ? extends V> entry : multimap.entries()) {
            this.valueToKey.put(entry.getValue(), entry.getKey());
        }

        return keyToValues.putAll(multimap);
    }

    @Override
    @CanIgnoreReturnValue
    public Collection<V> replaceValues(@Nullable K key, Iterable<? extends V> iterable) {
        Collection<V> replaced = this.keyToValues.replaceValues(key, iterable);
        for (V value : replaced) {
            this.valueToKey.remove(value);
        }

        for (V value : iterable) {
            this.valueToKey.put(value, key);
        }

        return replaced;
    }

    @Override
    @CanIgnoreReturnValue
    public Collection<V> removeAll(@Nullable Object key) {
        Collection<V> removed = keyToValues.removeAll(key);
        for (V value : removed) {
            this.valueToKey.remove(value);
        }

        return removed;
    }

    @Override
    public void clear() {
        keyToValues.clear();
        valueToKey.clear();
    }

    @Override
    public Collection<V> get(@Nullable K k) {
        return keyToValues.get(k);
    }

    @Override
    public Set<K> keySet() {
        return keyToValues.keySet();
    }

    @Override
    public Multiset<K> keys() {
        return keyToValues.keys();
    }

    @Override
    public Collection<V> values() {
        return keyToValues.values();
    }

    @Override
    public Collection<Entry<K, V>> entries() {
        return keyToValues.entries();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        keyToValues.forEach(action);
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return keyToValues.asMap();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return keyToValues.equals(o);
    }

    @Override
    public int hashCode() {
        return keyToValues.hashCode();
    }
}
