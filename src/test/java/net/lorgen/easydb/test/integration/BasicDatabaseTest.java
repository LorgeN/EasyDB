package net.lorgen.easydb.test.integration;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.response.Response;
import net.lorgen.easydb.test.TestItem;
import net.lorgen.easydb.test.integration.connection.redis.EnvironmentSQLConfig;
import net.lorgen.easydb.test.integration.connection.redis.RedisTestRepository;
import net.lorgen.easydb.test.integration.connection.sql.EnvironmentRedisConfig;
import net.lorgen.easydb.test.integration.connection.sql.SQLTestRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicDatabaseTest {

    @Parameter
    public ConnectionConfiguration configuration;

    @Test
    public void testAConfigRegister() {
        ConnectionRegistry registry = ConnectionRegistry.getInstance();
        registry.registerConfiguration(this.configuration);

        ItemRepository<TestItem> repository = this.getRepository();
        Truth.assertThat(repository).isNotNull();
    }

    @Test
    public void testBSaveFindUpdateDelete() {
        ItemRepository<TestItem> repository = this.getRepository();

        Truth.assertThat(repository).isNotNull();

        for (TestItem item : this.getTestItems(10)) {
            repository.newQuery()
              .set(item)
              .saveSync();

            Response<TestItem> found = repository.newQuery()
              .where().keysAre(item)
              .closeAll().findFirstSync();

            Truth.assertThat(found.isEmpty()).isFalse();
            Truth.assertThat(found.getInstance()).isEqualTo(item);

            String oldUsername = item.getUsername();
            String newUsername = this.randomString();

            repository.newQuery()
              .set("username", newUsername)
              .where().keysAre(item).closeAll()
              .saveSync();

            item.setUsername(newUsername);

            found = repository.newQuery()
              .where().keysAre(item)
              .closeAll().findFirstSync();

            Truth.assertThat(found.isEmpty()).isFalse();
            Truth.assertThat(found.getInstance()).isEqualTo(item);

            found = repository.newQuery()
              .where().equals("username", oldUsername).closeAll()
              .findFirstSync();

            Truth.assertThat(found.isEmpty()).isTrue();

            repository.newQuery().where().keysAre(item).closeAll().deleteSync();
        }
    }

    @Test
    public void testCReset() {
        this.getRepository().newQuery().deleteSync(); // Delete everything

        ConnectionRegistry.getInstance().closeAllAndReset();
    }

    private ItemRepository<TestItem> getRepository() {
        switch (this.configuration.getType()) {
            case SQL:
                return SQLTestRepository.getInstance();
            case MONGODB:
                return null;
            case REDIS:
                return RedisTestRepository.getInstance();
            default:
                return null;
        }
    }

    private List<TestItem> getTestItems(int count) {
        List<TestItem> list = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            list.add(new TestItem(this.randomString(), this.randomString(), this.randomString(), this.randomString(), i + 5 * 4));
        }

        return list;
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
