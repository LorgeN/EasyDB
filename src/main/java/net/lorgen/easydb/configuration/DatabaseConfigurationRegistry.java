package net.lorgen.easydb.configuration;

import com.google.common.collect.Maps;
import net.lorgen.easydb.DatabaseType;

import java.util.Map;

public class DatabaseConfigurationRegistry {

    private static DatabaseConfigurationRegistry instance;

    static {
        instance = new DatabaseConfigurationRegistry();
    }

    public static DatabaseConfigurationRegistry getInstance() {
        return instance;
    }

    // TODO: 1 connection pool per database? Not really sure if this would have
    // any performance deficits, but I think having a single pool per database
    // would be better RAM wise, and it would be a cleaner solution. Of course
    // this pool would have to be larger than the local ones

    private Map<DatabaseType, ConnectionConfiguration> connectionConfigsByType;

    private DatabaseConfigurationRegistry() {
        this.connectionConfigsByType = Maps.newHashMap();
    }

    public void registerConfiguration(ConnectionConfiguration configuration) {
        this.connectionConfigsByType.put(configuration.getType(), configuration);
    }

    public <T extends ConnectionConfiguration> T getConfiguration(DatabaseType type) {
        return (T) this.connectionConfigsByType.get(type);
    }
}
