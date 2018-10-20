package net.lorgen.easydb.test.integration.tests;

import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.test.TestItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class DatabaseSelect2IT {

    @Parameter(0)
    public StorageManager<TestItem> manager;

    @Parameter(1)
    public TestItem item;

    @Test
    public void selectAfterDelete() {
        TestItem selected = this.manager.newQuery()
          .where()
          .keysAre(this.item)
          .closeAll()
          .findFirstSync();

        // Shouldn't exist any more
        assertThat(selected).isNull();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return DatabaseTestSuite.CASES;
    }
}
