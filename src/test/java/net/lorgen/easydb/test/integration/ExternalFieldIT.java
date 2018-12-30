package net.lorgen.easydb.test.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.test.integration.connection.redis.EnvironmentSQLConfig;
import net.lorgen.easydb.test.integration.connection.sql.EnvironmentRedisConfig;
import net.lorgen.easydb.test.item.ExternalFieldItem;
import net.lorgen.easydb.test.item.NoKeyItem;
import net.lorgen.easydb.test.item.TestItem;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class ExternalFieldIT {

    @Parameter
    public ConnectionConfiguration configuration;

    @Test
    public void testExternalFields() {
        ConnectionRegistry registry = ConnectionRegistry.getInstance();
        registry.registerConfiguration(this.configuration);

        ItemRepository<ExternalFieldItem> repository = Repositories.createRepository(null, this.configuration.getType(), "external_test", ExternalFieldItem.class, null, null);

        assertThat(repository).isNotNull();

        for (int i = 0; i < 10; i++) {
            ExternalFieldItem item = new ExternalFieldItem(this.randomString());

            item.setItem(TestItem.getRandom());

            List<TestItem> itemList = Lists.newArrayList();
            List<NoKeyItem> itemList2 = Lists.newArrayList();
            int count = 10;
            for (int i1 = 0; i1 < count; i1++) {
                itemList.add(TestItem.getRandom());
                itemList2.add(NoKeyItem.getRandom());
            }

            item.setList(itemList);
            item.setList2(itemList2);

            Map<Integer, TestItem> map = Maps.newHashMap();
            Map<Integer, NoKeyItem> map2 = Maps.newHashMap();
            for (int i1 = 0; i1 < count; i1++) {
                TestItem someItem = TestItem.getRandom();
                map.put(i1, someItem);
                map2.put(i1, NoKeyItem.getRandom());
            }

            item.setMap(map);
            item.setMap2(map2);

            repository.newQuery()
              .set(item)
              .saveSync();
        }
    }

    private String randomString() {
        return RandomStringUtils.randomAlphabetic(ThreadLocalRandom.current().nextInt(6) + 5);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          new Object[]{new EnvironmentRedisConfig()},
          new Object[]{new EnvironmentSQLConfig()}
        );
    }
}