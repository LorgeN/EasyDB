package net.lorgen.easydb.test.sql;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.StorageManager;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.configuration.ConnectionConfiguration;
import net.lorgen.easydb.test.TestItem;

public class SQLTestManager extends StorageManager<TestItem> {

    private static final ConnectionConfiguration TEST_CONFIG;
    private static final SQLTestManager instance;

    static {
        TEST_CONFIG = new SQLConfiguration("localhost", "database", "username", "password", 3306);
        instance = new SQLTestManager();
    }

    public static SQLTestManager getInstance() {
        return instance;
    }

    private SQLTestManager() {
        super(TEST_CONFIG, "sql_test", TestItem.class, DatabaseType.SQL);
    }
}
