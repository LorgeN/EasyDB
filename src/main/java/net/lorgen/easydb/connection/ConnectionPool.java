package net.lorgen.easydb.connection;

import net.lorgen.easydb.DatabaseType;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;

import java.io.Closeable;

public interface ConnectionPool<T> extends Closeable {

    ConnectionConfiguration getConfiguration();

    DatabaseType getType();

    T getConnection();
}
