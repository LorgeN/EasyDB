package org.tanberg.easydb.util;

import org.tanberg.easydb.EasyDB;
import org.tanberg.easydb.field.FieldValue;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class UtilLog {

    public static String format(FieldValue<?>[] values) {
        StringBuilder builder = new StringBuilder("[");
        for (FieldValue<?> value : values) {
            if (builder.length() > 1) {
                builder.append(", ");
            }

            builder.append(value.getField().getName()).append(" = ").append(value.getValue());
        }

        return builder.append("]").toString();
    }

    private static Logger getLogger() {
        return EasyDB.getConfiguration().getLogger();
    }

    public static void severe(String msg) {
        getLogger().severe(msg);
    }

    public static void warning(String msg) {
        getLogger().warning(msg);
    }

    public static void info(String msg) {
        if (!EasyDB.getConfiguration().isVerbose()) {
            return;
        }

        getLogger().info(msg);
    }

    public static void config(String msg) {
        getLogger().config(msg);
    }

    public static void fine(String msg) {
        getLogger().fine(msg);
    }

    public static void finer(String msg) {
        getLogger().finer(msg);
    }

    public static void finest(String msg) {
        getLogger().finest(msg);
    }

    public static void log(LogRecord record) {
        getLogger().log(record);
    }

    public static void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    public static void log(Level level, Supplier<String> msgSupplier) {
        getLogger().log(level, msgSupplier);
    }

    public static void log(Level level, String msg, Object param1) {
        getLogger().log(level, msg, param1);
    }

    public static void log(Level level, String msg, Object[] params) {
        getLogger().log(level, msg, params);
    }

    public static void log(Level level, String msg, Throwable thrown) {
        getLogger().log(level, msg, thrown);
    }

    public static void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        getLogger().log(level, thrown, msgSupplier);
    }
}
