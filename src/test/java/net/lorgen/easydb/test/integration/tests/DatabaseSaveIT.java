package net.lorgen.easydb.test.integration.tests;

import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.test.TestItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class DatabaseSaveIT {

    @Parameter(0)
    public StorageManager<TestItem> manager;

    @Parameter(1)
    public TestItem item;

    @Test
    public void saveTest() {
        this.manager.newQuery()
          .set(this.item)
          .saveSync();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return DatabaseTestSuite.CASES;
    }
}
