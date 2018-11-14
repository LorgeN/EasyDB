package net.lorgen.easydb.connection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ConnectionRegistry {

    private static final Map<DatabaseType, Class<? extends ConnectionPool<?>>> POOL_CLASSES = ImmutableMap.<DatabaseType, Class<? extends ConnectionPool<?>>>builder()
      .build();

    private static ConnectionRegistry instance;

    static {
        instance = new ConnectionRegistry();
    }

    public static ConnectionRegistry getInstance() {
        return instance;
    }

    private Map<DatabaseType, ConnectionConfiguration> connectionConfigsByType;
    private Map<ConnectionConfiguration, ConnectionPool<?>> poolsByConfiguration;

    private ConnectionRegistry() {
        this.connectionConfigsByType = Maps.newHashMap();
        this.poolsByConfiguration = Maps.newHashMap();
    }

    public void registerConfiguration(ConnectionConfiguration configuration) {
        this.connectionConfigsByType.put(configuration.getType(), configuration);
    }

    public <T extends ConnectionConfiguration> T getConfiguration(DatabaseType type) {
        return (T) this.connectionConfigsByType.get(type);
    }

    public <T> ConnectionPool<T> getPool(ConnectionConfiguration configuration) {
        return (ConnectionPool<T>) this.poolsByConfiguration.computeIfAbsent(configuration, config -> {
            try {
                Class<? extends ConnectionPool<T>> poolClass = (Class<? extends ConnectionPool<T>>) POOL_CLASSES.get(config);
                Constructor<ConnectionPool<T>> constructor = (Constructor<ConnectionPool<T>>) poolClass.getConstructor(ConnectionConfiguration.class);

                return constructor.newInstance(config);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public void closeAll() {
        for (ConnectionPool<?> pool : this.poolsByConfiguration.values()) {
            try {
                pool.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
