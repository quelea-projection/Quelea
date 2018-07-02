package org.quelea.utils;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 * A wrapper for the Java desktop API that does the operations in a new thread, 
 * thus avoiding the freezes generated on some operating systems otherwise.
 * @author Michael
 */
public class ThreadedDesktop {
    
    public static void browse(String uri, ExceptionRunnable onError) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (Exception ex) {
                onError.run(ex);
            }
        }).start();
    }
    
    public static void open(File file, ExceptionRunnable onError) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                onError.run(ex);
            }
        }).start();
    }
    
    public static void print(File file, ExceptionRunnable onError) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().print(file);
            } catch (Exception ex) {
                onError.run(ex);
            }
        }).start();
    }
    
}
