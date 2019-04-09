package org.tanberg.easydb.connection;

import org.tanberg.easydb.DatabaseType;
import org.tanberg.easydb.connection.configuration.ConnectionConfiguration;

import java.io.Closeable;

public interface ConnectionPool<T> extends Closeable {

    ConnectionConfiguration getConfiguration();

    DatabaseType getType();

    T getConnection();
}
