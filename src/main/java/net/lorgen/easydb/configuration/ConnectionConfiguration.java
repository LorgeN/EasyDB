package net.lorgen.easydb.configuration;

import com.google.common.collect.Maps;
import net.lorgen.easydb.DatabaseType;

import java.util.Map;

public abstract class ConnectionConfiguration {

    private DatabaseType type;
    private Map<String, Object> options;

    public ConnectionConfiguration(DatabaseType type) {
        this(type, Maps.newHashMap());
    }

    public ConnectionConfiguration(DatabaseType type, Map<String, Object> options) {
        this.type = type;
        this.options = options;
    }

    public DatabaseType getType() {
        return type;
    }

    protected <T> T getValue(String key) {
        return (T) this.options.get(key);
    }

    protected void setValue(String key, Object value) {
        this.options.put(key, value);
    }
}
