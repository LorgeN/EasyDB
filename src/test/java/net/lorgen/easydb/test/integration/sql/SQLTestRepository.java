package net.lorgen.easydb.test.integration.sql;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.test.TestItem;

public class SQLTestRepository extends ItemRepository<TestItem> {


    private static final ConnectionConfiguration TEST_CONFIG;
    private static final SQLTestRepository instance;

    static {
        TEST_CONFIG = new SQLConfiguration("localhost", "test", "root", "", 3306);
        instance = new SQLTestRepository();
    }

    public static SQLTestRepository getInstance() {
        return instance;
    }

    private SQLTestRepository() {
        super(TEST_CONFIG, "sql_test", TestItem.class, DatabaseType.SQL);
    }
}
