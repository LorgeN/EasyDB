package net.lorgen.easydb.access.redis;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.configuration.ConnectionConfiguration;
import org.apache.commons.lang3.Validate;

public class RedisConfiguration extends ConnectionConfiguration {

    private static final String HOST_KEY = "host";
    private static final String PASSWORD_KEY = "password";
    private static final String PORT_KEY = "port";

    public RedisConfiguration(String host, String password, int port) {
        super(DatabaseType.REDIS);

        Validate.notBlank(host);
        Validate.notBlank(password);

        this.setValue(HOST_KEY, host);
        this.setValue(PASSWORD_KEY, password);
        this.setValue(PORT_KEY, port);
    }

    public String getHost() {
        return this.getValue(HOST_KEY);
    }

    public String getPassword() {
        return this.getValue(PASSWORD_KEY);
    }

    public int getPort() {
        return this.getValue(PORT_KEY);
    }
}
