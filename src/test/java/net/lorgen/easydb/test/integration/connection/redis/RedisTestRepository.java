package net.lorgen.easydb.test.integration.connection.redis;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.SimpleRepository;
import net.lorgen.easydb.test.TestItem;

public class RedisTestRepository extends SimpleRepository<TestItem> {

    private static final RedisTestRepository instance;

    static {
        instance = new RedisTestRepository();
    }

    public static RedisTestRepository getInstance() {
        return instance;
    }

    private RedisTestRepository() {
        super("redis_test", TestItem.class, DatabaseType.REDIS);
    }
}