/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.presentation.powerpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.quelea.services.utils.LoggerUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.quelea.services.utils.QueleaProperties;

/**
 * The Windows PowerPoint and PowerPoint Viewer control, through the VBScripts
 * in the vbs folder and key events. Used by {@link WindowsPowerPointBackend};
 * moved here from the old PowerPointHandler with the macOS code taken out
 * (that is in {@link MacPowerPointBackend} now).
 *
 * @author Arvid
 */
final class WindowsPowerPointScripts {

    private static String title;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Open a presentation with PowerPoint or PowerPoint Viewer
     *
     * @param path The absolute path to the presentation, needs to be surounded
     * with ""
     * @return Message whether the command was successful
     */
    static String openPresentation(String path) {
        if (path.contains("\\")) {
            title = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
        } else if (path.contains("/")) {
            title = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        }
        String ppPath = QueleaProperties.get().getPPPath();
        String ret;
        if (ppPath.contains("PPTVIEW")) {
            ret = pptViewOpen(ppPath, path);
        } else {
            ret = sendVBSSignal(getVBScriptPath("openPresentation.vbs") + " " + path);
            returnFocusToQuelea();
            focusPowerPoint();
        }
        return ret;
    }

    /**
     * Close any open PowerPoint window (NOTE: might produce undesired behaviour
     * if other presentations are opened)
     *
     * @return Message whether the command was successful
     */
    static String closePresentation() {
        String ret = "";
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            pptViewClose();
        } else {
            ret = sendVBSSignal(getVBScriptPath("closePresentation.vbs"));
        }
        return ret;
    }

    /**
     * Go to next slide/part of slide of the active presentation
     *
     * @return Message whether the command was successful
     */
    static String gotoNext() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return pptViewNext();
        }
        return sendVBSSignal(getVBScriptPath("gotoNext.vbs"));
    }

    /**
     * Go to previous slide/part of slide of the active presentation
     *
     * @return Message whether the command was successful
     */
    static String gotoPrevious() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return pptViewPrevious();
        }
        return sendVBSSignal(getVBScriptPath("gotoPrevious.vbs"));
    }

    /**
     * Go to a certain slide in the active presentation
     *
     * @param slideNumber The desired slide number to display
     * @return Message whether the command was successful
     */
    static String gotoSlide(int slideNumber) {
        String ret = "";
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            pptViewGoToSlide(slideNumber);
        } else {
            if (screenStatus().equals("")) {
                ret = sendVBSSignal(getVBScriptPath("startPresentation.vbs") + " " + slideNumber);
            } else {
                ret = sendVBSSignal(getVBScriptPath("gotoSlide.vbs") + " " + slideNumber);
            }
        }
        return ret;
    }

    /**
     * Get the active slide in the active presentation
     *
     * @return The number of the active slide as a string ("" with PowerPoint
     * Viewer, which cannot tell)
     */
    static String getCurrentSlide() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return ""; // Cannot be done with PowerPoint Viewer as far as I know
        }
        return sendVBSSignal(getVBScriptPath("getCurrentSlide.vbs"));
    }

    /**
     * Allow looping of the active presentation. This won't work with PowerPoint
     * Viewer, so that must be manually activated.
     *
     * @return Message whether the command was successful
     */
    static String loopPresentation() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return ""; // No method yet
        }
        return sendVBSSignal(getVBScriptPath("startLoop.vbs"));
    }

    /**
     * Stop looping the active presentation
     *
     * @return Message whether the command was successful
     */
    static String stopLoop() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return ""; // No method yet
        }
        return sendVBSSignal(getVBScriptPath("stopLoop.vbs"));
    }

    /**
     * Toggle black screen on the active presentation
     *
     * @return Message whether the command was successful
     */
    static String screenBlack() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return pptViewBlack();
        }
        return sendVBSSignal(getVBScriptPath("screenBlack.vbs"));
    }

    /**
     * Get screen status of the active presentation
     *
     * @return "2" for normal, "3" for black, "" if not started (always "" with
     * PowerPoint Viewer, which cannot tell)
     */
    static String screenStatus() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return ""; // This cannot be done with PowerPoint Viewer
        }
        return sendVBSSignal(getVBScriptPath("screenStatus.vbs"));
    }

    /**
     * Move focus to the PowerPoint window.
     *
     * @return Message whether the command was successful
     */
    static String focusPowerPoint() {
        if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
            return focusPPTView();
        }
        return sendVBSSignal(getVBScriptPath("focus.vbs"));
    }

    /*--------------------- Windows VBScript methods -----------------------*/
    /**
     * Method to handle all VBScript calls
     *
     * @param string Script to be called
     * @return Message whether the command was successful or desired value
     */
    private static String sendVBSSignal(String string) {
        String result = "";
        try {
            String scr = "cscript " + string;
            LOGGER.log(Level.FINE, "Running VBS: {0}", scr);
            Process p = Runtime.getRuntime().exec(scr.split(" "));
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    if (!line.contains("(C)") && !line.contains("(R)")) {
                        result += line;
                        if (line.contains("vbscript")) {
                            LOGGER.log(Level.INFO, "VBScript not found: {0}", line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Could not start VBScript " + string, e);
        }
        return result.trim();
    }

    /**
     * Get the path of the VBScript files
     *
     * @param script the name of the file
     * @return full path to the script
     */
    private static String getVBScriptPath(String script) {
        String path;
        if (script.contains(" ")) {
            script = script.replace(" ", "\" ");
            path = "\"" + new File("vbs/" + script).getAbsolutePath();
        } else {
            path = "\"" + new File("vbs/" + script).getAbsolutePath() + "\"";
        }

        return path;
    }

    /*--------------------- Windows PowerPoint Viewer methods -----------------------*/
    /**
     * Open a presentation with PowerPoint Viewer in Windows.
     *
     * @param ppPath The path to the PowerPoint Viwer file
     * @param path The path to the presentation
     * @return Message whether the command was successful
     */
    private static String pptViewOpen(String ppPath, String path) {
        String ret = "";
        try {
            // Always close old presentations before opening new ones
            pptViewClose();
            Thread.sleep(500);
            Runtime rt = Runtime.getRuntime();
            rt.exec(("\"" + ppPath + "\" /F" + path).split(" "));
            Thread.sleep(500);
            for (int i = 0; i < QueleaProperties.get().getProjectorScreen(); i++) {
                movePPTViewToSecondScreen();
            }
        } catch (IOException e) {
            ret = "Could not open PowerPoint Viewer ";
            LOGGER.log(Level.INFO, ret, e);
        } catch (InterruptedException ex) {
            ret = "Interrupted when trying to start PowerPoint Viewer ";
            LOGGER.log(Level.INFO, ret, ex);
        }
        return ret;
    }

    /**
     * Close PowerPoint Viewer in Windows
     */
    private static void pptViewClose() {
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(("taskkill /F /IM PPTVIEW.exe").split(" "));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could not close PowerPoint Viewer ", ex);
        }
    }

    /**
     * Send next signal through key event
     */
    private static String pptViewNext() {
        return sendPPTViewSignals(KeyEvent.VK_RIGHT);
    }

    /**
     * Send previous signal through key event
     */
    private static String pptViewPrevious() {
        return sendPPTViewSignals(KeyEvent.VK_LEFT);
    }

    /**
     * Send black signal through key event
     */
    private static String pptViewBlack() {
        return sendPPTViewSignals(KeyEvent.VK_B);
    }

    /**
     * Go to a certain slide through key events
     */
    private static void pptViewGoToSlide(int num) {
        LinkedList<Integer> stack = new LinkedList<>();
        while (num > 0) {
            stack.push(num % 10);
            num = num / 10;
        }

        // Set focus to PowerPoint presentation and send key signal number + return
        HWND hwnd = User32.INSTANCE.FindWindow(null,
                title); // window title
        if (hwnd == null) {
            LOGGER.log(Level.INFO, "PowerPoint Viewer is not running");
        } else {
            User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
            User32.INSTANCE.SetForegroundWindow(hwnd); // bring to front
            Robot robot;
            try {
                robot = new Robot();
                while (!stack.isEmpty()) {
                    int slide = stack.pop() + 96;
                    robot.keyPress(slide);
                    robot.keyRelease(slide);
                }
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            } catch (AWTException e) {
                LOGGER.log(Level.INFO, "Failed going to slide {0} in PowerPoint Viewer", num);
            }
        }
    }

    /**
     * Manually move the PowerPoint Viewer window through key events. It will
     * show the presentation on the first screen by default for some reason.
     * Note: This only moves the window to the right, so screens extended to the
     * left are not supported.
     */
    private static void movePPTViewToSecondScreen() {
        // Set focus to PowerPoint Viewer and send key signal "win+shift+right"
        HWND hwnd = null;
        int tries = 0;
        do {
            hwnd = User32.INSTANCE.FindWindow(null,
                    title); // window title
            tries++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "Interrupted when trying to find PowerPoint Viewer");
            }
        } while (tries != 10 && hwnd == null);
        if (hwnd == null) {
            LOGGER.log(Level.INFO, "PowerPoint Viewer is not running");
        } else {
            User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
            User32.INSTANCE.SetForegroundWindow(hwnd); // bring to front
            Robot robot;
            try {
                robot = new Robot();
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_WINDOWS);
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                robot.keyRelease(KeyEvent.VK_WINDOWS);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } catch (AWTException e) {
                LOGGER.log(Level.INFO, "Failed moving PowerPoint Viewer to projection monitor");
            }
            returnFocusToQuelea();
        }
    }

    /**
     * Method for sending key events to PowerPoint Viewer in Windows.
     *
     * @param keyEvent the decired signal to send to PowerPoint Viewer
     */
    private static String sendPPTViewSignals(int keyEvent) {
        // Set focus to PowerPoint presentation and send key signals
        String ret;
        if (focusPowerPoint().contains("Successfully")) {
            Robot robot;
            try {
                robot = new Robot();
                robot.keyPress(keyEvent);
                robot.keyRelease(keyEvent);
            } catch (AWTException e) {
                LOGGER.log(Level.INFO, "Failed sendng key event to PowerPoint Viewer");
            }
            ret = "Signal sending was successful";
        } else {
            ret = "PowerPoint Viewer is not running";
        }
        return ret;
    }

    /**
     * Manually focus the PowerPoint Viewer window. Shift focus to PowerPoint
     * Viewer to move that window to the top or resume playback.
     */
    private static String focusPPTView() {
        // Set focus to PowerPoint Viewer
        HWND hwnd = User32.INSTANCE.FindWindow(null,
                title); // window title
        String ret = "";
        if (hwnd == null) {
            ret = "PowerPoint Viewer is not running";
            LOGGER.log(Level.INFO, ret);

        } else {
            User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
            User32.INSTANCE.SetForegroundWindow(hwnd); // bring to front
            ret = "Successfully focued PPTView";
        }
        return ret;
    }

    /*--------------------- General methods -----------------------*/
    /**
     * Moves the focus back to the Quelea window.
     *
     * Note: PowerPoint (both regual and Viewer) stops any ongoing playback or
     * animation when focus is lost, so perhaps this feature should be
     * evaualted.
     */
    private static void returnFocusToQuelea() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, "Interrupted when trying to move focus back to Quelea");
        }
        HWND quelea = User32.INSTANCE.FindWindow(null,
                "Quelea " + QueleaProperties.VERSION.getVersionString()); // Move back to main window
        if (quelea == null) {
            LOGGER.log(Level.INFO, "Could not find Quelea main window");
        } else {
            User32.INSTANCE.SetForegroundWindow(quelea); // bring to front
        }
    }
}
