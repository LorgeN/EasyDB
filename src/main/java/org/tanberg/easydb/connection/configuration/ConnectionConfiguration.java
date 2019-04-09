package org.tanberg.easydb.connection.configuration;

import com.google.common.collect.Maps;
import org.tanberg.easydb.DatabaseType;

import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConnectionConfiguration)) {
            return false;
        }

        ConnectionConfiguration that = (ConnectionConfiguration) o;
        return type == that.type &&
          Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, options);
    }

    @Override
    public String toString() {
        return "ConnectionConfiguration{" +
          "type=" + type +
          ", options=" + options +
          '}';
    }
}
