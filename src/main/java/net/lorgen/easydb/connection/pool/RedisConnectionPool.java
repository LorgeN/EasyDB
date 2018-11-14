package net.lorgen.easydb.connection.pool;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.access.redis.RedisConfiguration;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.connection.ConnectionPool;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

public class RedisConnectionPool implements ConnectionPool<Jedis> {

    private final JedisPool pool;
    private final RedisConfiguration configuration;

    public RedisConnectionPool(ConnectionConfiguration configuration) {
        if (!(configuration instanceof SQLConfiguration)) {
            throw new IllegalArgumentException("Not an SQL configuration!");
        }

        RedisConfiguration config = this.configuration = (RedisConfiguration) configuration;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(1000);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(20);

        String host = config.getHost();
        int port = config.getPort();
        String password = (config.getPassword() == null || config.getPassword().isEmpty()) ? null : config.getPassword();

        this.pool = new JedisPool(poolConfig, host, port, 2000, password);
    }

    @Override
    public ConnectionConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.REDIS;
    }

    @Override
    public Jedis getConnection() {
        return pool.getResource();
    }

    @Override
    public void close() throws IOException {
        this.pool.close();
    }
}
