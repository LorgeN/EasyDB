package org.tanberg.easydb.test.integration.connection.redis;

import org.tanberg.easydb.access.sql.SQLConfiguration;

public class EnvironmentSQLConfig extends SQLConfiguration {

    private static final String PROPERTY_PREFIX = "sql.";

    public EnvironmentSQLConfig() {
        super(System.getenv(PROPERTY_PREFIX + HOST_KEY),
          System.getenv(PROPERTY_PREFIX + DATABASE_KEY),
          System.getenv(PROPERTY_PREFIX + USER_KEY),
          System.getenv(PROPERTY_PREFIX + PASSWORD_KEY),
          Integer.valueOf(System.getenv(PROPERTY_PREFIX + PORT_KEY)));
    }
}
