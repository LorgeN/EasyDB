package net.lorgen.easydb.access.sql;

public class SQLConfig {

    private String host;
    private String database;
    private String user;
    private String password;
    private int port;

    public SQLConfig(String host, String database, String user, String password, int port) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }
}
