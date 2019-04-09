package org.tanberg.easydb;

import org.tanberg.easydb.configuration.EasyDBConfiguration;

public final class EasyDB {

    // Avoid instantiation
    private EasyDB() {
    }

    private static final EasyDBConfiguration CONFIGURATION = new EasyDBConfiguration();

    public static EasyDBConfiguration getConfiguration() {
        return CONFIGURATION;
    }

    public <T> T make(Class<T> tClass) {
        if (!tClass.isInterface()) {
            throw new IllegalArgumentException("Please provide an interface!");
        }

        return null; // TODO
    }
}
