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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Provides some static utility methods to do with logging.
 * <p/>
 * @author Michael
 */
public final class LoggerUtils {

    /**
     * The default level for loggers.
     */
    public static final Level DEFAULT_LEVEL = Level.INFO;
    private static final Map<String, Logger> loggers;
    private static volatile File handlerFile;

    static {
        loggers = new HashMap<>();
    }

    /**
     * Initialise the handlers.
     */
    private static void initialise() {
        if(handlerFile == null) {
            synchronized(LoggerUtils.class) {
                if(handlerFile == null) {
                    handlerFile = Utils.getDebugLog();
                    handlerFile.delete();
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

    public static String getHandlerFileLocation() {
        return handlerFile.getAbsolutePath();
    }

    /**
     * Get a logger with its appropriate class name.
     * <p/>
     * @return a logger that uses the called class as its name.
     */
    public static synchronized Logger getLogger() {
        initialise();
        StackTraceElement[] ele = new Throwable().getStackTrace();
        String name;
        if(ele == null || ele.length < 3 || ele[2] == null || ele[2].getClassName() == null) {
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
                    synchronized(this) {
                        writeToLog(new SimpleFormatter().format(record));
                    }
                    if(record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        StringBuilder sendText = new StringBuilder();
                        sendText.append(record.getLevel().getName()).append("\n");
                        sendText.append(new SimpleFormatter().format(record)).append("\n");
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
            loggers.put(name, logger);
        }
        return logger;
    }

    private static void writeToLog(String message) {
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(handlerFile, true)))) {
            out.println(message);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
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
}
