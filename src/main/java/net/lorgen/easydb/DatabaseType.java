package net.lorgen.easydb;

import net.lorgen.easydb.access.redis.RedisAccessor;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.access.sql.SQLAccessor;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.configuration.ConnectionConfiguration;
import net.lorgen.easydb.configuration.DatabaseConfigurationRegistry;

public enum DatabaseType {
    SQL {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, StorageManager<T> manager, String tableName) {
            return new SQLAccessor<>((SQLConfiguration) configuration, manager, tableName);
        }
    },
    MONGODB {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, StorageManager<T> manager, String tableName) {
            return null; // TODO
        }
    },
    REDIS {
        @Override
        public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, StorageManager<T> manager, String tableName) {
            return new RedisAccessor<>((RedisConfiguration) configuration, manager, tableName);
        }
    };

    public abstract <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, StorageManager<T> manager, String tableName);

    public <T extends StoredItem> DatabaseTypeAccessor<T> newAccessor(StorageManager<T> manager, String tableName) {
        ConnectionConfiguration configuration = DatabaseConfigurationRegistry.getInstance().getConfiguration(this);
        if (configuration == null) {
            throw new IllegalArgumentException("Missing configuration for database type " + this.name() + "!");
        }

        return this.newAccessor(configuration, manager, tableName);
    }
}
