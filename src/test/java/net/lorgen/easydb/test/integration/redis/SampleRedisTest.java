package net.lorgen.easydb.test.integration.redis;

import com.github.fppt.jedismock.RedisServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SampleRedisTest {

    private RedisServer redisServer;

    @Before
    public void before() throws IOException {
        redisServer = RedisServer.newRedisServer();  // bind to a random port
        redisServer.start();
    }

    @Test
    public void test() {
        Jedis jedis = new Jedis(redisServer.getHost(), redisServer.getBindPort());
        jedis.set("testKey", "testValue");

        assertEquals(jedis.get("testKey"), "testValue");
    }

    @After
    public void after() {
        redisServer.stop();
        redisServer = null;
    }

}
