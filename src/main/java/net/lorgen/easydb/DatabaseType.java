package net.lorgen.easydb;

import net.lorgen.easydb.access.redis.RedisAccessor;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.access.sql.SQLAccessor;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.configuration.DatabaseConfigurationRegistry;

public enum DatabaseType {
    SQL {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName) {
            SQLConfiguration configuration = DatabaseConfigurationRegistry.getInstance().getConfiguration(SQL);
            if (configuration == null) {
                throw new IllegalStateException("No configuration registered for database type SQL!");
            }

            return new SQLAccessor<>(configuration, manager, tableName);
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
            RedisConfiguration configuration = DatabaseConfigurationRegistry.getInstance().getConfiguration(REDIS);
            if (configuration == null) {
                throw new IllegalStateException("No configuration registered for database type REDIS!");
            }

            return new RedisAccessor<>(configuration, manager, tableName);
        }
    };

    public abstract <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName);
}
