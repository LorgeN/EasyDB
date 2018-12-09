package net.lorgen.easydb.test.integration.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.assertEquals;

public class SampleH2Test {

    private Connection dbConnection;

    @Before
    public void setup() throws SQLException {
        dbConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;", "sa", null);
    }


    @Test
    public void sampleTest() throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.execute("create table test (id long)");

        dbConnection.commit();

        Statement statement2 = dbConnection.createStatement();
        statement2.execute("insert into test values ('123')");

        dbConnection.commit();

        Statement assertStatement = dbConnection.createStatement();
        ResultSet resultSet = assertStatement.executeQuery("select * from test");

        while(resultSet.next()) {
            assertEquals(123, resultSet.getLong("id"));
        }

    }

    @After
    public void tearDown() throws SQLException {
        dbConnection.close();
    }
}
