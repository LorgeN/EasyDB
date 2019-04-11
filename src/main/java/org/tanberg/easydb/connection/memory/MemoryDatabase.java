package org.tanberg.easydb.connection.memory;

import com.google.common.collect.Maps;
import org.tanberg.easydb.profile.ItemProfile;

import java.util.Map;

public class MemoryDatabase {

    private static final Map<String, MemoryTable<?>> NAME_TO_TABLE = Maps.newConcurrentMap();

    public static <T> MemoryTable<T> getTable(String table, ItemProfile<T> profile) {
        return (MemoryTable<T>) NAME_TO_TABLE.computeIfAbsent(table, str -> new MemoryTable<>(table, profile));
    }
}
