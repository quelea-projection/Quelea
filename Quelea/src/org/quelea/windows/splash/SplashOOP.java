/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.splash;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;
import org.quelea.windows.multimedia.advancedPlayer.OOPPlayer;

/**
 * Out of process splash screen. This allows UI updates to the splash screen
 * while UI threads are used by the main application while starting
 *
 * @author Greg
 */
public class SplashOOP extends Application {

    private static Process p;

    /**
     * The main method to start it out of process
     *
     * @param args Arguments (Ignored)
     */
    public static void main(String[] args) {
        Application.launch();
    }

    /**
     * Start the splash screen
     */
    public static void showStage() {
        ProcessBuilder pb = new ProcessBuilder();
        String fullClassName = SplashOOP.class.getName();
        String pathToClassFiles = new File("./build/classes").getPath();
        String pathSeparator = File.pathSeparator; // ":" on Linux, ";" on Windows
        String pathToLib = new File("./lib/jfxrt.jar").getPath();

        pb.command("java", "-cp", pathToLib + pathSeparator + pathToClassFiles, fullClassName, "myArg");
        try {

            final Process p = pb.start();
            SplashOOP.p = p;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    Scanner s = new Scanner(p.getErrorStream());
                    while (s.hasNext()) {
                        System.err.println(s.nextLine());
                    }
                }
            });
            t.start();

        } catch (IOException ex) {
            Logger.getLogger(OOPPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Close the splash screen
     */
    public static void hideStage() {
        try{
        p.destroy();
        }catch(Exception ex){
             Logger.getLogger(OOPPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts a splash screen out of process. This enables any animation on the
     * splash screen that one desires.
     *
     * @param stage Ignored
     */
    @Override
    public void start(Stage stage) {
        final SplashStage splashWindow = new SplashStage();
        splashWindow.showAndAnimate();
    }
}
