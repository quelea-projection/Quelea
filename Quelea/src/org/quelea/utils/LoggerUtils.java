package org.quelea.utils;

import java.io.IOException;
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
    private static final FileHandler FILE_HANDLER;

    static {
        try {
            FILE_HANDLER = new FileHandler("quelea-debug.txt");
            FILE_HANDLER.setFormatter(new SimpleFormatter());
        }
        catch(IOException ex) {
            //Can't really do a lot here
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
        final Logger logger = Logger.getLogger(new Throwable().getStackTrace()[1].getClassName());
        logger.setLevel(DEFAULT_LEVEL);
        logger.addHandler(FILE_HANDLER);
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
