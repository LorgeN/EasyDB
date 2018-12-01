package net.lorgen.easydb;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.access.redis.RedisAccessor;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.access.sql.SQLAccessor;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;

public enum DatabaseType {
    SQL {
        @Override
        public <T extends DatabaseTypeAccessor> boolean isAccessor(Class<T> accessor) {
            return SQLAccessor.class.isAssignableFrom(accessor);
        }

        @Override
        public <T> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, DatabaseRepository<T> manager, String tableName) {
            return new SQLAccessor<>((SQLConfiguration) configuration, manager, tableName);
        }
    },
    MONGODB {
        @Override
        public <T extends DatabaseTypeAccessor> boolean isAccessor(Class<T> accessor) {
            return false;
        }

        @Override
        public <T> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, DatabaseRepository<T> manager, String tableName) {
            return null; // TODO
        }
    },
    REDIS {
        @Override
        public <T extends DatabaseTypeAccessor> boolean isAccessor(Class<T> accessor) {
            return RedisAccessor.class.isAssignableFrom(accessor);
        }

        @Override
        public <T> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, DatabaseRepository<T> manager, String tableName) {
            return new RedisAccessor<>((RedisConfiguration) configuration, manager, tableName);
        }
    };

    public <T> boolean isAccessor(DatabaseTypeAccessor<T> accessor) {
        return this.isAccessor(accessor.getClass());
    }

    public abstract <T extends DatabaseTypeAccessor> boolean isAccessor(Class<T> accessor);

    public abstract <T> DatabaseTypeAccessor<T> newAccessor(ConnectionConfiguration configuration, DatabaseRepository<T> manager, String tableName);

    public <T> DatabaseTypeAccessor<T> newAccessor(DatabaseRepository<T> manager, String tableName) {
        ConnectionConfiguration configuration = ConnectionRegistry.getInstance().getConfiguration(this);
        if (configuration == null) {
            throw new IllegalArgumentException("Missing configuration for database type " + this.name() + "!");
        }

        return this.newAccessor(configuration, manager, tableName);
    }

    public static <T extends DatabaseTypeAccessor> DatabaseType fromAccessor(Class<T> accessor) {
        for (DatabaseType value : DatabaseType.values()) {
            if (!value.isAccessor(accessor)) {
                continue;
            }

            return value;
        }

        return null;
    }

    public static <T> DatabaseType fromAccessor(DatabaseTypeAccessor<T> accessor) {
        return fromAccessor(accessor.getClass());
    }
}
