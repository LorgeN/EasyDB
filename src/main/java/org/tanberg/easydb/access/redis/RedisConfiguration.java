package org.tanberg.easydb.access.redis;

import org.tanberg.easydb.DatabaseType;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class RedisConfiguration extends ConnectionConfiguration {

    protected static final String HOST_KEY = "host";
    protected static final String PASSWORD_KEY = "password";
    protected static final String PORT_KEY = "port";

    public RedisConfiguration(String host, String password, int port) {
        super(DatabaseType.REDIS);

        Validate.notBlank(host);

        this.setValue(HOST_KEY, host);
        this.setValue(PORT_KEY, port);

        if (!StringUtils.isBlank(password)) {
            this.setValue(PASSWORD_KEY, password);
        }
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
