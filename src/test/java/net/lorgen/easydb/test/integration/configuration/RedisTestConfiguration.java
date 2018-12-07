package net.lorgen.easydb.test.integration.configuration;

import net.lorgen.easydb.access.redis.RedisConfiguration;

public class RedisTestConfiguration extends RedisConfiguration {

    private static final String PROPERTY_PREFIX = "redis.";

    public RedisTestConfiguration() {
        super(System.getProperty(PROPERTY_PREFIX + HOST_KEY),
          System.getProperty(PROPERTY_PREFIX + PASSWORD_KEY),
          Integer.valueOf(System.getProperty(PROPERTY_PREFIX + PORT_KEY)));
    }
}
