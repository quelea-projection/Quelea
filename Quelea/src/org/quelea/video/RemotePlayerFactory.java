package org.quelea.video;

import com.sun.jna.Native;
import java.awt.Canvas;
import java.io.IOException;

public class RemotePlayerFactory {
    
    private static int portCounter = 5555;

    public static RemotePlayer getEmbeddedRemotePlayer(Canvas canvas) {
        try {
            long drawable = Native.getComponentID(canvas);
            StreamWrapper wrapper = startSecondPlayerJVM(OutOfProcessEmbeddedPlayer.class, Long.toString(drawable));
            final RemotePlayer player = new RemotePlayer(wrapper);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    player.close();
                }
            });
            return player;
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't create embedded remote player", ex);
        }
    }
    
    public static int getNextPort() {
        return portCounter;
    }
    
    public static RemotePlayer getHeadlessRemotePlayer() {
        try {
            StreamWrapper wrapper = startSecondPlayerJVM(OutOfProcessHeadlessPlayer.class, Integer.toString(portCounter++));
            final RemotePlayer player = new RemotePlayer(wrapper);
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    player.close();
                }
            });
            return player;
        }
        catch (IOException ex) {
            throw new RuntimeException("Couldn't create headless remote player", ex);
        }
    }

    private static StreamWrapper startSecondPlayerJVM(Class<? extends OutOfProcessPlayer> clazz, String option) throws IOException {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, "-Djna.library.path=" + System.getProperty("jna.library.path"), clazz.getName(), option);
        Process process = processBuilder.start();
        return new StreamWrapper(process.getInputStream(), process.getOutputStream());
    }
}
