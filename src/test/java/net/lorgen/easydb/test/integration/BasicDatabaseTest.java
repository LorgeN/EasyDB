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

import static com.google.common.truth.Truth.*;

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
        assertThat(repository).isNotNull();
    }

    @Test
    public void testBSaveFindUpdateDelete() {
        ItemRepository<TestItem> repository = this.getRepository();

        assertThat(repository).isNotNull();

        for (TestItem item : this.getTestItems(10)) {
            // Save the item to the repository
            repository.newQuery()
              .set(item)
              .saveSync();

            // Try fetching the item
            Response<TestItem> found = repository.newQuery()
              .where().keysAre(item)
              .closeAll().findFirstSync();

            // Verify we actually found something
            assertThat(found.isEmpty()).isFalse();
            // Verify we found the correct thing
            assertThat(found.getInstance()).isEqualTo(item);

            // Store the old username
            String oldUsername = item.getUsername();
            // Generate a new username
            String newUsername = this.randomString();

            // Update the value in the database
            repository.newQuery()
              .set("username", newUsername)
              .where().keysAre(item).closeAll()
              .saveSync();

            // Update the local instance
            item.setUsername(newUsername);

            // Fetch the new value from the database
            found = repository.newQuery()
              .where().keysAre(item)
              .closeAll().findFirstSync();

            // Assert that we found something
            assertThat(found.isEmpty()).isFalse();
            // Assert that it was actually updated
            assertThat(found.getInstance()).isEqualTo(item);

            // Try finding an item with the old username
            found = repository.newQuery()
              .where().equals("username", oldUsername).closeAll()
              .findFirstSync();

            // Assert that no such item exists
            assertThat(found.isEmpty()).isTrue();

            // Delete the item
            repository.newQuery().where().keysAre(item).closeAll().deleteSync();

            // Try to find it again
            found = repository.newQuery()
              .where().keysAre(item)
              .closeAll().findFirstSync();

            // Assert that nothing was found
            assertThat(found.isEmpty()).isTrue();
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
