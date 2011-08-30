package org.quelea.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Provides some static utility methods to do with logging.
 * @author Michael
 */
public final class LoggerUtils {

    /**
     * The default level for loggers.
     */
    public static final Level DEFAULT_LEVEL = Level.INFO;
    private static volatile FileHandler FILE_HANDLER;
    private static final Map<String, Logger> loggers;

    static {
        loggers = new HashMap<>();
    }

    private static void initialise() {
        if (FILE_HANDLER == null) {
            synchronized (LoggerUtils.class) {
                if (FILE_HANDLER == null) {
                    try {
                        File handler = new File(QueleaProperties.getQueleaUserHome(), "quelea-debuglog.txt");
                        FILE_HANDLER = new FileHandler(handler.getAbsolutePath());
                        FILE_HANDLER.setFormatter(new SimpleFormatter());
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                        //Can't really do a lot here
                    }
                }
            }
        }
    }

    /**
     * No instantiation for me thanks.
     */
    private LoggerUtils() {
        throw new AssertionError();
    }

    /**
     * Get a logger with its appropriate class name.
     * @return a logger that uses the called class as its name.
     */
    public static Logger getLogger() {
        return getLogger(true);
    }

    /**
     * Get a logger with its appropriate class name.
     * @param file true if the logger should write to the debug file, false
     * otherwise. Should only be false with out of process loggers...
     * @return a logger that uses the called class as its name.
     */
    public static Logger getLogger(boolean file) {
        initialise();
        StackTraceElement[] ele = new Throwable().getStackTrace();
        String name;
        if (ele == null || ele[1] == null || ele[1].getClassName() == null) {
            name = "DEFAULT";
        }
        else {
            name = ele[1].getClassName();
        }
        Logger logger = loggers.get(name);
        if (logger == null) {
            logger = Logger.getLogger(name);
            logger.setLevel(DEFAULT_LEVEL);

            try {
                logger.addHandler(FILE_HANDLER);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            loggers.put(name, logger);
        }
        return logger;
    }

    /**
     * Determine if we were able to write to the file handler or not. If not then the debug log won't be written.
     * @return true if all is ok, false if there is a problem.
     */
    public boolean isFileHandlerOK() {
        return FILE_HANDLER != null;
    }
}
