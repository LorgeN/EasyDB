package net.lorgen.easydb.test.sql;

import net.lorgen.easydb.test.TestItem;
import org.junit.Test;

public class SQLSaveTest {

    @Test
    public void saveTest() {
        // String username, String firstName, String lastName, String email, int age
        TestItem item = new TestItem("TestUser", "John", "Doe", "johndoe@foo.com", 35);

        SQLTestManager.getInstance().newQuery()
          .set(item)
          .saveSync();
    }
}
