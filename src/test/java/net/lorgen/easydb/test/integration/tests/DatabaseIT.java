package net.lorgen.easydb.test.integration.tests;

import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.SimpleRepository;
import net.lorgen.easydb.test.TestItem;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DatabaseIT {

    @Parameter(0)
    public SimpleRepository<TestItem> manager;

    @Parameter(1)
    public TestItem item;

    @Test
    public void testASave() {
        this.manager.newQuery()
          .set(this.item)
          .saveSync();
    }

    @Test
    public void testBSelectAfterInsert() {
        TestItem selected = this.manager.newQuery()
          .where()
          .equals("name", this.item.getUsername())
          .closeAll()
          .findFirstSync().getInstance();

        // Assert that we found it
        assertThat(selected).isNotNull();

        ItemProfile<TestItem> profile = this.manager.getProfile();
        for (PersistentField<TestItem> field : profile.getFields()) {
            assertThat(field.getValue(selected)).isEqualTo(field.getValue(this.item));
        }
    }

    @Test
    public void testCUpdate() {
        this.manager.newQuery()
          .set("firstName", "Updated John")
          .where()
          .equals("name", this.item.getUsername())
          .closeAll()
          .saveSync();

        TestItem item = this.manager.newQuery()
          .where()
          .equals("name", this.item.getUsername())
          .closeAll()
          .findFirstSync().getInstance();

        assertThat(item.getFirstName()).isEqualTo("Updated John");
    }

    @Test
    public void testDDelete() {
        this.manager.newQuery()
          .where().keysAre(this.item)
          .closeAll().deleteSync();
    }

    @Test
    public void testESelectAfterDelete() {
        TestItem selected = this.manager.newQuery()
          .where()
          .keysAre(this.item)
          .closeAll()
          .findFirstSync().getInstance();

        // Shouldn't exist any more
        assertThat(selected).isNull();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return DatabaseTestSuite.CASES;
    }
}
