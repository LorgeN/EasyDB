package org.tanberg.easydb.configuration;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EasyDBConfiguration {

    public static final SimpleDateFormat LOGGER_OUTPUT_FORMAT = new SimpleDateFormat("hh:mm:ss");

    private boolean verbose = false;
    private boolean inMemoryOnly = false;
    private Logger logger = Logger.getLogger("EasyDB");

    public EasyDBConfiguration() {
        this.logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                String header = "[" + LOGGER_OUTPUT_FORMAT.format(new Date()) + " " + record.getLevel().getName() + "] ";
                String message = header + record.getMessage();

                Throwable throwable = record.getThrown();
                if (throwable != null) {
                    message += "\n" + ExceptionUtils.getStackTrace(throwable);
                }

                return message + "\n";
            }
        });

        this.logger.addHandler(handler);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isInMemoryOnly() {
        return inMemoryOnly;
    }

    public void setInMemoryOnly(boolean inMemoryOnly) {
        this.inMemoryOnly = inMemoryOnly;
    }
}
