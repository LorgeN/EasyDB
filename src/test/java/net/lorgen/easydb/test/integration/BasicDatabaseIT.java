package net.lorgen.easydb.test.integration;

import com.google.common.collect.Lists;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.Repositories;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.response.Response;
import net.lorgen.easydb.test.integration.connection.redis.EnvironmentSQLConfig;
import net.lorgen.easydb.test.integration.connection.sql.EnvironmentRedisConfig;
import net.lorgen.easydb.test.item.TestItem;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class BasicDatabaseIT {

    @Parameter
    public ConnectionConfiguration configuration;

    @Test
    public void testBasicOperations() {
        ItemRepository<TestItem> repository = Repositories.createRepository(this.configuration, null, "basic_test", TestItem.class, null, null);

        assertThat(repository).isNotNull();

        for (TestItem item : this.getTestItems()) {
            // Save the item to the repository
            repository.newQuery()
              .set(item)
              .saveSync();

            // Try fetching the item
            Response<TestItem> found = repository.newQuery()
              .where().keysAreSameAs(item)
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
              .where().keysAreSameAs(item).closeAll()
              .saveSync();

            // Update the local instance
            item.setUsername(newUsername);

            // Fetch the new value from the database
            found = repository.newQuery()
              .where().keysAreSameAs(item)
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

            // Make some local changes
            item.setEmail(this.randomString());
            item.setLastName(this.randomString());

            // Save it
            repository.newQuery()
              .set(item)
              .saveSync();

            // Fetch the new value from the database
            found = repository.newQuery()
              .where().keysAreSameAs(item)
              .closeAll().findFirstSync();

            // Assert that we found something
            assertThat(found.isEmpty()).isFalse();
            // Assert that it was actually updated
            assertThat(found.getInstance()).isEqualTo(item);

            // Delete the item
            repository.newQuery().where().keysAreSameAs(item).closeAll().deleteSync();

            // Try to find it again
            found = repository.newQuery()
              .where().keysAreSameAs(item)
              .closeAll().findFirstSync();

            // Assert that nothing was found
            assertThat(found.isEmpty()).isTrue();
        }

        repository.newQuery().deleteSync(); // Delete everything

        ConnectionRegistry.getInstance().closeAllAndReset();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Lists.newArrayList(
          new Object[]{new EnvironmentRedisConfig()},
          new Object[]{new EnvironmentSQLConfig()}
        );
    }

    // Internals

    private List<TestItem> getTestItems() {
        List<TestItem> list = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            list.add(TestItem.getRandom());
        }

        return list;
    }

    private String randomString() {
        return RandomStringUtils.randomAlphabetic(ThreadLocalRandom.current().nextInt(6) + 5);
    }
}
