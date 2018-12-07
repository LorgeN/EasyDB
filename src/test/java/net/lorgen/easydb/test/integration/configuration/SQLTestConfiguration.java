package net.lorgen.easydb.test.integration.configuration;

import net.lorgen.easydb.access.sql.SQLConfiguration;

public class SQLTestConfiguration extends SQLConfiguration {

    private static final String PROPERTY_PREFIX = "sql.";

    public SQLTestConfiguration() {
        super(System.getProperty(PROPERTY_PREFIX + HOST_KEY),
          System.getProperty(PROPERTY_PREFIX + DATABASE_KEY),
          System.getProperty(PROPERTY_PREFIX + USER_KEY),
          System.getProperty(PROPERTY_PREFIX + PASSWORD_KEY),
          Integer.valueOf(System.getProperty(PROPERTY_PREFIX + PORT_KEY)));
    }
}
