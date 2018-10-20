package net.lorgen.easydb.test.integration.tests;

import com.google.common.collect.Lists;
import net.lorgen.easydb.StorageManager;
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
public class DatabaseSetupIT {

    @Parameter
    public StorageManager<TestItem> manager;

    @Test
    public void testADrop() {
        this.manager.getDatabaseAccessor().drop();
    }

    @Test
    public void testBSetUp() {
        this.manager.getDatabaseAccessor().setUp();
    }

    @Test
    public void testCAssertReset() {
        List<TestItem> list = this.manager.newQuery().findAllSync();

        assertThat(list).isEmpty();
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> cases = Lists.newArrayList();
        for (StorageManager<TestItem> manager : DatabaseTestSuite.MANAGERS) {
            cases.add(new Object[]{manager});
        }

        return cases;
    }
}
