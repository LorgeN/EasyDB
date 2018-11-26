package net.lorgen.easydb.test.integration.tests;

import com.google.common.collect.Lists;
import net.lorgen.easydb.SimpleRepository;
import net.lorgen.easydb.test.TestItem;
import net.lorgen.easydb.test.integration.redis.RedisTestRepository;
import net.lorgen.easydb.test.integration.sql.SQLTestRepository;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.util.List;

@RunWith(Suite.class)
@SuiteClasses({
  DatabaseSetupIT.class,
  DatabaseIT.class,
  DatabaseUniqueIT.class
})
public class DatabaseTestSuite {

    public static final TestItem TEST_ITEM_1 = new TestItem("TestUser1", "John", "Doe", "johndoe@foo.com", 35);
    public static final TestItem TEST_ITEM_2 = new TestItem("TestUser2", "John", "Foo", "johnfoo@foo.net", 34);
    public static final TestItem TEST_ITEM_3 = new TestItem("TestUser3", "John", "Loo", "johnloo@foo.org", 33);
    public static final TestItem TEST_ITEM_4 = new TestItem("TestUser4", "John", "Sue", "johnsue@foo.ru", 32);
    public static final TestItem TEST_ITEM_5 = new TestItem("TestUser5", "John", "Tim", "johntim@foo.co.uk", 31);

    public static final TestItem[] ITEMS = {TEST_ITEM_1, TEST_ITEM_2, TEST_ITEM_3, TEST_ITEM_4, TEST_ITEM_5};
    public static final SimpleRepository<TestItem>[] MANAGERS = new SimpleRepository[]{SQLTestRepository.getInstance(), RedisTestRepository.getInstance()};
    public static final List<Object[]> CASES;

    static {
        List<Object[]> cases = Lists.newArrayList();

        for (SimpleRepository<TestItem> manager : MANAGERS) {
            for (TestItem item : ITEMS) {
                cases.add(new Object[]{manager, item});
            }
        }

        CASES = cases;
    }
}
