package net.lorgen.easydb.access.redis;

public class RedisConfig {

    private String host;
    private String password;
    private int port;

    public RedisConfig(String host, String password, int port) {
        this.host = host;
        this.password = password;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }
}
