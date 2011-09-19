/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.video;

import com.sun.jna.Native;
import java.awt.Canvas;
import java.io.IOException;

/**
 * Factory class responsible for creating the out of process video players and
 * providing remote objects to control them.
 * @author Michael
 */
public class RemotePlayerFactory {
    
    private static int portCounter = 5555;

    /**
     * Get a remote embedded player, pointing to an embedded player in another
     * VM.
     * @param canvas the canvas ID (got using JNA) that the other player should
     * use.
     * @return the remote embedded player.
     */
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
    
    /**
     * Get the next available port to use for the next headless player.
     * @return the next available port number.
     */
    public static int getNextPort() {
        return portCounter;
    }
    
    /**
     * Get a headless remote player interface to a player in a separate VM.
     * @return a headless remote player interface to a player in a separate VM.
     */
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

    /**
     * Start a second JVM to control an out of process player.
     * @param clazz the type of out of process player we're launching (must be
     * a class with a main method.)
     * @param option any options to pass to VLCJ.
     * @return a stream wrapper object for controlling the second VM.
     * @throws IOException if something goes wrong.
     */
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
