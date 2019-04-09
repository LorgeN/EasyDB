package org.tanberg.easydb.connection.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.tanberg.easydb.DatabaseType;
import org.tanberg.easydb.access.sql.SQLConfiguration;
import org.tanberg.easydb.connection.ConnectionPool;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnectionPool implements ConnectionPool<Connection> {

    private final HikariDataSource dataSource;
    private final SQLConfiguration configuration;

    public SQLConnectionPool(ConnectionConfiguration configuration) {
        if (!(configuration instanceof SQLConfiguration)) {
            throw new IllegalArgumentException("Not an SQL configuration!");
        }

        SQLConfiguration sqlConfig = this.configuration = (SQLConfiguration) configuration;

        String host = sqlConfig.getHost();
        int port = sqlConfig.getPort();
        String database = sqlConfig.getDatabase();
        String user = sqlConfig.getUser();
        String password = sqlConfig.getPassword();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&serverTimezone=UTC");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(10000);
        hikariConfig.setConnectionTimeout(10000);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public ConnectionConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQL;
    }

    @Override
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        this.dataSource.close();
    }
}
