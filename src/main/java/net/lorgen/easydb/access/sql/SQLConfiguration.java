package net.lorgen.easydb.access.sql;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class SQLConfiguration extends ConnectionConfiguration {

    protected static final String HOST_KEY = "host";
    protected static final String DATABASE_KEY = "database";
    protected static final String USER_KEY = "user";
    protected static final String PASSWORD_KEY = "password";
    protected static final String PORT_KEY = "port";

    public SQLConfiguration(String host, String database, String user, String password, int port) {
        super(DatabaseType.SQL);

        Validate.notBlank(host);
        Validate.notBlank(database);
        Validate.notBlank(user);

        this.setValue(HOST_KEY, host);
        this.setValue(DATABASE_KEY, database);
        this.setValue(USER_KEY, user);
        this.setValue(PORT_KEY, port);

        if (!StringUtils.isBlank(password)) {
            this.setValue(PASSWORD_KEY, password);
        }
    }

    public String getHost() {
        return this.getValue(HOST_KEY);
    }

    public String getDatabase() {
        return this.getValue(DATABASE_KEY);
    }

    public String getUser() {
        return this.getValue(USER_KEY);
    }

    public String getPassword() {
        return this.getValue(PASSWORD_KEY);
    }

    public int getPort() {
        return this.getValue(PORT_KEY);
    }
}
