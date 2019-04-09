package org.tanberg.easydb.test.integration.connection.sql;

import org.tanberg.easydb.access.redis.RedisConfiguration;

public class EnvironmentRedisConfig extends RedisConfiguration {

    private static final String PROPERTY_PREFIX = "redis.";

    public EnvironmentRedisConfig() {
        super(System.getenv(PROPERTY_PREFIX + HOST_KEY),
          System.getenv(PROPERTY_PREFIX + PASSWORD_KEY),
          Integer.valueOf(System.getenv(PROPERTY_PREFIX + PORT_KEY)));
    }
}