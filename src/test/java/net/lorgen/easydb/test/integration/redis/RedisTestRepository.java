package net.lorgen.easydb.test.integration.redis;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.test.TestItem;

public class RedisTestRepository extends ItemRepository<TestItem> {

    private static final ConnectionConfiguration TEST_CONFIG;
    private static final RedisTestRepository instance;

    static {
        TEST_CONFIG = new RedisConfiguration("localhost", "", 6379);
        instance = new RedisTestRepository();
    }

    public static RedisTestRepository getInstance() {
        return instance;
    }

    private RedisTestRepository() {
        super(TEST_CONFIG, "redis_test", TestItem.class, DatabaseType.REDIS);
    }
}