package org.quelea.video;

import com.sun.jna.Native;
import java.awt.Canvas;

public class RemotePlayerFactory {

    public static RemotePlayer getRemotePlayer(Canvas canvas) {
        try {
            long drawable = Native.getComponentID(canvas);
            StreamWrapper wrapper = startSecondJVM(drawable);
            final RemotePlayer player = new RemotePlayer(wrapper);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    player.close();
                }
            });
            return player;
        }
        catch (Exception ex) {
            throw new RuntimeException("Couldn't create remote player", ex);
        }
    }

    private static StreamWrapper startSecondJVM(long drawable) throws Exception {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, "-Djna.library.path=" + System.getProperty("jna.library.path"), OutOfProcessEmbeddedPlayer.class.getName(), Long.toString(drawable));
        Process process = processBuilder.start();
        return new StreamWrapper(process.getInputStream(), process.getOutputStream());
    }
}
