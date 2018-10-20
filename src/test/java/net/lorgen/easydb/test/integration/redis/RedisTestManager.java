package net.lorgen.easydb.test.integration.redis;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.configuration.ConnectionConfiguration;
import net.lorgen.easydb.test.TestItem;

public class RedisTestManager extends StorageManager<TestItem> {

    private static final ConnectionConfiguration TEST_CONFIG;
    private static final RedisTestManager instance;

    static {
        TEST_CONFIG = new RedisConfiguration("localhost", "", 6379);
        instance = new RedisTestManager();
    }

    public static RedisTestManager getInstance() {
        return instance;
    }

    private RedisTestManager() {
        super(TEST_CONFIG, "redis_test", TestItem.class, DatabaseType.REDIS);
    }
}