/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Provides some static utility methods to do with logging.
 *
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

    /**
     * Initialise the handlers.
     */
    private static void initialise() {
        if(FILE_HANDLER == null) {
            synchronized(LoggerUtils.class) {
                if(FILE_HANDLER == null) {
                    try {
                        File handler = new File(QueleaProperties.getQueleaUserHome(), "quelea-debuglog.txt");
                        FILE_HANDLER = new FileHandler(handler.getAbsolutePath());
                        FILE_HANDLER.setFormatter(new SimpleFormatter());
                    }
                    catch(IOException ex) {
//                        ex.printStackTrace();
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
     *
     * @return a logger that uses the called class as its name.
     */
    public static Logger getLogger() {
        return getLogger(true);
    }

    /**
     * Get a logger with its appropriate class name.
     *
     * @param file true if the logger should write to the debug file, false
     * otherwise. Should only be false with out of process loggers...
     * @return a logger that uses the called class as its name.
     */
    public static synchronized Logger getLogger(boolean file) {
        initialise();
        StackTraceElement[] ele = new Throwable().getStackTrace();
        String name;
        if(ele == null || ele[2] == null || ele[2].getClassName() == null) {
            name = "DEFAULT";
        }
        else {
            name = ele[2].getClassName();
        }
        Logger logger = loggers.get(name);
        if(logger == null) {
            logger = Logger.getLogger(name);
            logger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if(record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        StringBuilder sendText = new StringBuilder();
                        sendText.append(record.getLevel().getName()).append("\n");
                        sendText.append(record.getMessage()).append("\n");
                        if(record.getThrown() != null) {
                            for(StackTraceElement e : record.getThrown().getStackTrace()) {
                                sendText.append(e.toString()).append("\n");
                            }
                        }
                        String errorText = null;
                        try {
                            errorText = URLEncoder.encode(sendText.toString(), "UTF-8");
                        }
                        catch(UnsupportedEncodingException ex) {
                            //Nothing much here
                        }
                        final String fiErrorText = errorText;
                        new Thread() {
                            @Override
                            public void run() {
                                phoneHomeError(fiErrorText);
                            }
                        }.start();
                    }
                }

                @Override
                public void flush() {
                    //Norhing needed here
                }

                @Override
                public void close() throws SecurityException {
                    //Norhing needed here
                }
            });
            logger.setLevel(DEFAULT_LEVEL);
            try {
                logger.addHandler(FILE_HANDLER);
            }
            catch(Exception ex) {
//                ex.printStackTrace();
            }
            loggers.put(name, logger);
        }
        return logger;
    }

    private static void phoneHomeError(String error) {
        if(!QueleaProperties.get().getPhoneHome()) {
            return;
        }

        String os = System.getProperty("os.name") + " : " + System.getProperty("os.version");

        final StringBuilder urlStrBuilder = new StringBuilder("http://quelea.org/errorreport/store.php?os=");
        urlStrBuilder.append(os);
        urlStrBuilder.append("&version=");
        urlStrBuilder.append(QueleaProperties.VERSION.getVersionString());
        urlStrBuilder.append("&language=");
        urlStrBuilder.append(Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));

        String urlStr = urlStrBuilder.toString().replace(" ", "%20");
        urlStr += "&error=";
        urlStr += error;
        
        final StringBuilder result = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlStr).openStream()));) {
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        catch(IOException ex) {
            //Oh well.
        }

    }

    /**
     * Determine if we were able to write to the file handler or not. If not
     * then the debug log won't be written.
     *
     * @return true if all is ok, false if there is a problem.
     */
    public boolean isFileHandlerOK() {
        return FILE_HANDLER != null;
    }
}
