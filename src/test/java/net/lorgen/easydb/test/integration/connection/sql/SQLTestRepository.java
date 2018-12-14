package net.lorgen.easydb.test.integration.connection.sql;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.SimpleRepository;
import net.lorgen.easydb.access.sql.SQLConfiguration;
import net.lorgen.easydb.test.TestItem;

public class SQLTestRepository extends SimpleRepository<TestItem> {

    private static final SQLTestRepository instance;

    static {
        instance = new SQLTestRepository();
    }

    public static SQLTestRepository getInstance() {
        return instance;
    }

    private SQLTestRepository() {
        super("sql_test", TestItem.class, DatabaseType.SQL);
    }

    private SQLTestRepository(SQLConfiguration configuration) {
        super(configuration, "sql_test", TestItem.class, DatabaseType.SQL);
    }
}
