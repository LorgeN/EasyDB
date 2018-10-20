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
public class DatabaseDeleteIT {

    @Parameter(0)
    public StorageManager<TestItem> manager;

    @Parameter(1)
    public TestItem item;

    @Test
    public void deleteTest() {
        this.manager.newQuery()
          .where().keysAre(this.item)
          .closeAll().deleteSync();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return DatabaseTestSuite.CASES;
    }
}
