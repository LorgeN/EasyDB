package net.lorgen.easydb.test.sql;

import org.junit.Test;

public class SQLDeleteTest {

    @Test
    public void deleteTest() {
        SQLTestManager.getInstance().newQuery()
          .set("name", "John")
          .deleteSync();
    }
}
