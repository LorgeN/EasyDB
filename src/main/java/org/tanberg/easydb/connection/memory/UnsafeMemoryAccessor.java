package org.tanberg.easydb.connection.memory;

import org.tanberg.easydb.WrappedIndex;
import org.tanberg.easydb.util.ValueContainer;

import java.util.Map;

public class UnsafeMemoryAccessor<T> {

    private final Map<ValueContainer, ValueContainer> keyToValueMap;
    private final Map<WrappedIndex<T>, MemoryIndexMap<T>> indexMaps;

    public UnsafeMemoryAccessor(Map<ValueContainer, ValueContainer> keyToValueMap, Map<WrappedIndex<T>, MemoryIndexMap<T>> indexMaps) {
        this.keyToValueMap = keyToValueMap;
        this.indexMaps = indexMaps;
    }

    public Map<ValueContainer, ValueContainer> getKeyToValueMap() {
        return keyToValueMap;
    }

    public Map<WrappedIndex<T>, MemoryIndexMap<T>> getIndexMaps() {
        return indexMaps;
    }
}
