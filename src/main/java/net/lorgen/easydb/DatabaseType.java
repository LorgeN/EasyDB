package net.lorgen.easydb;

import net.lorgen.easydb.access.redis.RedisAccessor;
import net.lorgen.easydb.access.sql.SQLAccessor;

public enum DatabaseType {
    SQL {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName) {
            return new SQLAccessor<>(null, manager, tableName);
        }
    },
    MONGODB {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName) {
            return null; // TODO
        }
    },
    REDIS {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName) {
            return new RedisAccessor<>(null, manager, tableName);
        }
    };

    public abstract <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName);
}
