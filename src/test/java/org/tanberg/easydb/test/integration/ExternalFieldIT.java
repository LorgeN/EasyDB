package org.tanberg.easydb.test.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.Repositories;
import org.tanberg.easydb.configuration.EasyDBConfiguration;
import org.tanberg.easydb.connection.ConnectionRegistry;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;
import org.tanberg.easydb.query.response.Response;
import org.tanberg.easydb.test.integration.connection.redis.EnvironmentSQLConfig;
import org.tanberg.easydb.test.integration.connection.sql.EnvironmentRedisConfig;
import org.tanberg.easydb.test.item.ExternalFieldItem;
import org.tanberg.easydb.test.item.NoKeyItem;
import org.tanberg.easydb.test.item.TestItem;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class ExternalFieldIT {

    @Parameter
    public ConnectionConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        EasyDBConfiguration.getInstance().setVerbose(true);
    }

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
                itemList.add(TestItem.getRandom(item.getName()));
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
              .save();

            List<Response<TestItem>> testItems = Repositories.getRepository(this.configuration.getType(), "list_key_external_test", TestItem.class, null)
              .newQuery()
              .where()
              .equals("name", item.getName())
              .closeAll()
              .findAll();

            // Verify it got stored
            assertThat(testItems.stream().map(Response::getInstance).collect(Collectors.toList())).containsExactlyElementsIn(item.getList());

            Response<ExternalFieldItem> response = repository.newQuery()
              .where().keysAreSameAs(item).closeAll()
              .findFirst();

            assertThat(response.isEmpty()).isFalse();

            ExternalFieldItem fetched = response.getInstance();

            assertThat(fetched.getItemId()).isEqualTo(item.getItemId());
            assertThat(fetched.getName()).isEqualTo(item.getName());
            assertThat(fetched.getItem()).isEqualTo(item.getItem());

            assertThat(fetched.getList()).containsExactlyElementsIn(item.getList());
            assertThat(fetched.getList2()).containsExactlyElementsIn(item.getList2());

            System.out.println("Checking map #1:");
            assertThat(fetched.getMap()).containsExactlyEntriesIn(item.getMap());
            System.out.println("Checking map #2:");
            assertThat(fetched.getMap2()).containsExactlyEntriesIn(item.getMap2());

            assertThat(fetched).isEqualTo(item);
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
