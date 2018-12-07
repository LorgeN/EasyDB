package net.lorgen.easydb.test.integration.tests;

import com.google.common.collect.Lists;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.SimpleRepository;
import net.lorgen.easydb.response.Response;
import net.lorgen.easydb.test.TestItem;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DatabaseUniqueIT {

    public static final TestItem TEST_ITEM_1 = new TestItem("TestUser", "John", "Doe", "johndoe@foo.com", 35);
    public static final TestItem TEST_ITEM_2 = new TestItem("TestUser", "John", "Foo", "johnfoo@foo.net", 34);
    public static final TestItem TEST_ITEM_3 = new TestItem("TestUser", "John", "Foo", "johnfoo@foo.net", 34);

    @Parameter(0)
    public SimpleRepository<TestItem> manager;

    @Test
    public void testAInsertObject() {
        this.manager.newQuery().set(TEST_ITEM_1).saveSync();

        // Validate it's actually there
        this.validateEquals(TEST_ITEM_1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBInvalidObject() {
        this.manager.newQuery().set(TEST_ITEM_2).saveSync();

        // Validate it didn't update
        this.validateEquals(TEST_ITEM_1);
    }

    @Test
    public void testCValidObject() {
        TEST_ITEM_3.setId(TEST_ITEM_1.getId());
        this.manager.newQuery().set(TEST_ITEM_3).saveSync();

        // Validate it actually updated
        this.validateEquals(TEST_ITEM_3);
    }

    @Test
    public void testDCleanup() {
        this.manager.newQuery().where().equals("username", "TestUser").closeAll().deleteSync();
    }

    private void validateEquals(TestItem item) {
        List<Response<TestItem>> actualList = this.manager.newQuery().where().equals("username", item.getUsername()).closeAll().findAllSync();

        assertThat(actualList.size()).isEqualTo(1);

        TestItem actual = actualList.get(0).getInstance();

        assertThat(actual).isNotNull();

        ItemProfile<TestItem> profile = this.manager.getProfile();
        for (PersistentField<TestItem> field : profile.getFields()) {
            assertThat(field.getValue(actual)).isEqualTo(field.getValue(item));
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> cases = Lists.newArrayList();
        for (SimpleRepository<TestItem> manager : DatabaseTestSuite.MANAGERS) {
            cases.add(new Object[]{manager});
        }

        return cases;
    }
}
