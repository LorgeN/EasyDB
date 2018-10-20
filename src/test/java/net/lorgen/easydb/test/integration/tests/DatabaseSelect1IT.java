package net.lorgen.easydb.test.integration.tests;

import net.lorgen.easydb.PersistentField;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.StoredItemProfile;
import net.lorgen.easydb.test.TestItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class DatabaseSelect1IT {

    @Parameter(0)
    public StorageManager<TestItem> manager;

    @Parameter(1)
    public TestItem item;

    @Test
    public void selectAfterInsert() {
        TestItem selected = this.manager.newQuery()
          .where()
          .equals("name", this.item.getUsername())
          .closeAll()
          .findFirstSync();

        // Assert that we found it
        assertThat(selected).isNotNull();

        StoredItemProfile<TestItem> profile = this.manager.getProfile();
        for (PersistentField<TestItem> field : profile.getFields()) {
            assertThat(field.getValue(selected)).isEqualTo(field.getValue(this.item));
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return DatabaseTestSuite.CASES;
    }
}
