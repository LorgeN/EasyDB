package net.lorgen.easydb.test.sql;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  SQLSaveTest.class,
  SQLDeleteTest.class
})
public class SQLSuite {

    @Before
    public void setUp() throws Exception {
        SQLTestManager.getInstance(); // Instantiate
    }
}
