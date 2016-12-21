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
package org.quelea.windows.presentation;

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
import javafx.application.Platform;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * Class to send OS-native signals to control a PowerPoint presentation
 *
 * @author Arvid
 */
public class PowerPointHandler {

    private static String title;
    private static boolean loop;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Open a presentation with PowerPoint or PowerPoint Viewer
     *
     * @param path The absolute path to the presentation, needs to be surounded
     * with ""
     * @return Message whether the command was successful
     */
    public static String openPresentation(String path) {
        if (path.contains("\\")) {
            title = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
        } else if (path.contains("/")) {
            title = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        }
        String ppPath = QueleaProperties.get().getPPPath();
        if (QueleaProperties.get().getUsePP()) {
            if (Utils.isMac()) {
                String ret = "";
                if (path.contains(" ")) {
                    path = path.replaceFirst("/", "").replaceAll("/", ":");
                    ret = loadAppleScript(getAppleScript("open2").replace("filepath", path));
                } else {
                    ret = loadAppleScript(getAppleScript("open").replace("filepath", path));
                }
//                returnFocusToQuelea();
                return ret;
            } else if (Utils.isWindows() && new File(ppPath).exists()) {
                String ret = "";
                if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                    ret = pptViewOpen(ppPath, path);
                } else {
                    ret = sendVBSSignal(getVBScriptPath("openPresentation.vbs") + " " + path);
                    returnFocusToQuelea();
                    focusPowerPoint();
                    return ret;
                }
            }
        } else {
            return "PowerPoint was not found";
        }
        return "";
    }

    /**
     * Close any open PowerPoint window (NOTE: might produce undesired behaviour
     * if other presentations are opened)
     *
     * @return Message whether the command was successful
     */
    public static String closePresentation() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("close"));
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                pptViewClose();
            } else {
                ret = sendVBSSignal(getVBScriptPath("closePresentation.vbs"));
            }
        }
        return ret;
    }

    /**
     * Go to next slide/part of slide of the active presentation
     *
     * @return Message whether the command was successful
     */
    public static String gotoNext() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("next"));
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                ret = pptViewNext();
            } else {
                ret = sendVBSSignal(getVBScriptPath("gotoNext.vbs"));
                focusPowerPoint();
            }
        }
        return ret;
    }

    /**
     * Go to previous slide/part of slide of the active presentation
     *
     * @return Message whether the command was successful
     */
    public static String gotoPrevious() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("previous"));
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                ret = pptViewPrevious();
            } else {
                ret = sendVBSSignal(getVBScriptPath("gotoPrevious.vbs"));
                focusPowerPoint();
            }
        }
        return ret;
    }

    /**
     * Go to a certain slide in the active presentation
     *
     * @param slideNumber The desired slide number to display
     * @return Message whether the command was successful
     */
    public static String gotoSlide(int slideNumber) {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("gotoSlide").replace("{0}", String.valueOf(slideNumber))); // Is done with key event signals as it doesn't seem to be possible to do with AppleScript
//            returnFocusToQuelea();
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                pptViewGoToSlide(slideNumber);
            } else {
                ret = sendVBSSignal(getVBScriptPath("gotoSlide.vbs") + " " + slideNumber);
                focusPowerPoint();
            }
        }
        return ret;
    }

    /**
     * Get the active slide in the active presentation
     *
     * @return The number of the active slide as a string
     */
    public static String getCurrentSlide() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("getSlide")).trim();
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                // Cannot be done with PowerPoint Viewer as far as I know
            } else {
                ret = sendVBSSignal(getVBScriptPath("getCurrentSlide.vbs"));
            }
        }
        return ret;
    }

    /**
     * Get the total number of slides in the active presentation
     *
     * @return The total number of slides as a string
     */
    public static String getTotalSlides() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("getTotal")).trim();
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                // Cannot be done with PowerPoint Viewer as far as I know
            } else {
                ret = sendVBSSignal(getVBScriptPath("getTotalSlides.vbs"));
            }
        }
        return ret;
    }

    /**
     * Start looping the active presentation. This is suggested not to be used
     * but rather to use the native loop in Quelea along with the next signal.
     *
     * @param seconds The desired interval between each slide
     * @return Message whether the command was successful
     */
    public static String loopPresentation(int seconds) {
        String ret = "";
        if (Utils.isMac()) {
            // No method yet
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                /* 
                 Cannot be done with PowerPoint Viewer in a good way as far as I know
                 The method below will shift focus to the presentation at the set
                 interval, which might lead to a buggy expericene for the user.
                 */
                ret = "Note: This is an unstable feature";
                pptViewLoop(seconds);
            } else {
                return sendVBSSignal(getVBScriptPath("startLoop.vbs") + " " + seconds);
            }
        }
        return ret;
    }

    /**
     * Stop looping the active presentation
     *
     * @return Message whether the command was successful
     */
    public static String stopLoop() {
        String ret = "";
        if (Utils.isMac()) {
            // No method yet
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                ret = "Note: This is an unstable feature";
                setLoop(false);
            } else {
                return sendVBSSignal(getVBScriptPath("stopLoop.vbs"));
            }
        }
        return ret;
    }

    /**
     * Toggle black screen on the active presentation
     *
     * @return Message whether the command was successful
     */
    public static String screenBlack() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("black")); // Is done with key event signals as it doesn't seem to be possible to set slide state with AppleScript
//            returnFocusToQuelea();
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                return pptViewBlack();
            } else {
                return sendVBSSignal(getVBScriptPath("screenBlack.vbs"));
            }
        }
        return ret;
    }

    /**
     * Get screen status of the active presentation
     *
     * @return Message whether the command was successful
     */
    public static String screenStatus() {
        String ret = "";
        if (Utils.isMac()) {
            ret = loadAppleScript(getAppleScript("status")).trim();
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                // This cannot be done with PowerPoint Viewer
            } else {
                ret = sendVBSSignal(getVBScriptPath("screenStatus.vbs"));
            }
        }
        return ret;
    }

    /**
     * Create a PDF file of active presentation
     *
     * @param path the presentation that should be used
     * @return Message whether the command was successful
     */
    public static String createPDF(String path) {
        path = "\"" + path + "\"";
        if (path.contains("\\")) {
            title = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
        } else if (path.contains("/")) {
            title = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        }
        String ret = "";
        if (Utils.isMac()) {
            // TODO: Not supported yet
        } else if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                // This cannot be done with PowerPoint Viewer
            } else {
                File mainFolder = new File("icons/slides/" + title);
                if (!mainFolder.exists()) {
                    mainFolder.mkdir();
                }
                ret = sendVBSSignal(getVBScriptPath("exportToPDF.vbs") + " " + path + " \"" + mainFolder.getAbsolutePath() + "\\" + title + ".pdf\"");
            }
        }
        return ret;
    }

    /**
     * Move focus to the Quelea main window.
     */
    public static void focusQuelea() {
        QueleaApp.get().getMainWindow().toFront();
    }

    /**
     * Move focus to the PowerPoint window.
     *
     * @return Message whether the command was successful
     */
    public static String focusPowerPoint() {
        String ret = "";
        if (Utils.isWindows()) {
            if (QueleaProperties.get().getPPPath().contains("PPTVIEW")) {
                ret = focusPPTView();
            } else {
                ret = sendVBSSignal(getVBScriptPath("focus.vbs"));
            }
        } else {
            ret = loadAppleScript(getAppleScript("focus"));
        }
        return ret;
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
        Platform.runLater(() -> {
        });
        try {
            Process p = Runtime.getRuntime().exec("cscript " + string);
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
            path = "\"" + new File("lib/vbs/" + script).getAbsolutePath();
        } else {
            path = "\"" + new File("lib/vbs/" + script).getAbsolutePath() + "\"";
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
            rt.exec("\"" + ppPath + "\" /F" + path);
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
            rt.exec("taskkill /F /IM PPTVIEW.exe");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could not close PowerPoint Viewer ", ex);
        }
    }

    /**
     * Send next signal through key event
     */
    private static String pptViewNext() {
        return sendPPTViewSignals(KeyEvent.VK_DOWN);
    }

    /**
     * Send previous signal through key event
     */
    private static String pptViewPrevious() {
        return sendPPTViewSignals(KeyEvent.VK_UP);
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
            returnFocusToQuelea();
        }
    }

    /**
     * Experimental loop feature for PowerPoint Viewer through key event
     *
     * @param seconds Interval for slide progression
     */
    private static void pptViewLoop(int seconds) {
        // Set focus to PowerPoint presentation and send key signal "down"
        Thread thread = new Thread(() -> {
            while (isLoop()) {
                HWND hwnd = User32.INSTANCE.FindWindow(null,
                        title); // window title
                if (hwnd == null) {
                    LOGGER.log(Level.INFO, "PowerPoint Viewer is not running");
                    // Manually restart presentation?
//                    openPresentation(filePath);
                } else {
                    User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
                    User32.INSTANCE.SetForegroundWindow(hwnd); // bring to front
                    Robot robot;
                    try {
                        robot = new Robot();
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.delay(seconds * 1000);
                    } catch (AWTException ex) {
                        LOGGER.log(Level.INFO, "Failed looping in PowerPoint Viewer");
                    }
                }
                returnFocusToQuelea();
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Loop variable for experimental PowerPoint Viewer loop feature
     *
     * @return the loop
     */
    public static boolean isLoop() {
        return loop;
    }

    /**
     * Loop variable for experimental PowerPoint Viewer loop feature
     *
     * @param aLoop the loop to set
     */
    public static void setLoop(boolean aLoop) {
        loop = aLoop;
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
        if (focusPowerPoint().contains("successfully")) {
            Robot robot;
            try {
                robot = new Robot();
                robot.keyPress(keyEvent);
                robot.keyRelease(keyEvent);
            } catch (AWTException e) {
                LOGGER.log(Level.INFO, "Failed sendng key event to PowerPoint Viewer");
            }
            returnFocusToQuelea();
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

    /*--------------------- Mac AppleScript methods -----------------------*/
    /**
     * Get the result from the AppleScript.
     *
     * @param process the AppleScript Process
     * @return the return string from the AppleScript
     */
    private static String getMacResult(Process result) {
        try {
            result.waitFor();

            String line;
            StringBuilder output = new StringBuilder();

            if (result.exitValue() != 0) {

                BufferedReader err = new BufferedReader(new InputStreamReader(result.getErrorStream()));
                while ((line = err.readLine()) != null) {
                    output.append(line).append("\n");
                }

                return ("PowerPoint is not running: " + output.toString().trim());

            } else {
                String s;
                StringBuilder sb = new StringBuilder();
                BufferedReader res = new BufferedReader(new InputStreamReader(result.getInputStream()));
                while ((s = res.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                return sb.toString();
            }

        } catch (InterruptedException | IOException ex) {
            LOGGER.log(Level.INFO, "Failed getting result from AppleScript");
        }
        return "";
    }

    /**
     * The AppleScript methods for PowerPoint
     *
     * @param string The method to load
     * @return the AppleScript
     */
    private static String getAppleScript(String string) {
        switch (string) {
            case "open":
                return "tell application \"Microsoft PowerPoint\"\n"
                        + "     open filepath\n"
                        + "	repeat until document window 1 exists\n"
                        + "		delay 0.2\n"
                        + "	end repeat\n"
                        + "	\n"
                        + "	set theSlideShowSet to slide show settings of active presentation\n"
                        + "     try\n"
                        + "         set loop until stopped of theSlideShowSet to true\n"
                        + "         set show type of theSlideShowSet to slide show type presenter\n"
                        + "     on error errorMessage number errorNumber\n"
                        + "         return errorMessage\n"
                        + "     end try\n"
                        + "	run slide show theSlideShowSet\n"
                        + "end tell";
            case "close":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "        close active presentation saving no\n"
                        + "     end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "gotoSlide":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "  tell application \"Microsoft PowerPoint\"\n"
                        + "	activate\n"
                        + "	delay 0.25\n"
                        + "	tell application \"System Events\" to keystroke \"{0}\"\n"
                        + "	tell application \"System Events\" to key code 36\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "next":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "	go to next slide slideshow view of slide show window 1\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "previous":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "	go to previous slide slideshow view of slide show window 1\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "getSlide":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "     return slide index of slide of slide show view of slide show window 1\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "black":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "     activate\n"
                        + "	delay 0.25\n"
                        + "	tell application \"System Events\" to keystroke \"b\"\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "getTotal":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "	set maxSlide to (get count of slides of presentation of document window 1)\n"
                        + "     return maxSlide\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "status":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "     tell application \"Microsoft PowerPoint\"\n"
                        + "	if (slide state of slide show view of slide show window of active presentation is slide show state black screen) then\n"
                        + "		return \"3\"\n"
                        + "	else\n"
                        + "		return \"1\"\n"
                        + "	end if\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            case "open2":
                return "tell application \"Finder\"\n"
                        + "	open alias filepath\n"
                        + "end tell\n"
                        + "delay 2\n"
                        + "if application \"Microsoft PowerPoint\" is running then\n"
                        + "tell application \"Microsoft PowerPoint\"\n"
                        + "     open filepath\n"
                        + "	repeat until document window 1 exists\n"
                        + "		delay 0.2\n"
                        + "	end repeat\n"
                        + "	\n"
                        + "	set theSlideShowSet to slide show settings of active presentation\n"
                        + "     try\n"
                        + "         set loop until stopped of theSlideShowSet to true\n"
                        + "         set show type of theSlideShowSet to slide show type presenter\n"
                        + "     on error errorMessage number errorNumber\n"
                        + "         return errorMessage\n"
                        + "     end try\n"
                        + "	run slide show theSlideShowSet\n"
                        + "end tell\n"
                        + "else\n"
                        + "	delay 2\n"
                        + "tell application \"Microsoft PowerPoint\"\n"
                        + "     open filepath\n"
                        + "	repeat until document window 1 exists\n"
                        + "		delay 0.2\n"
                        + "	end\n"
                        + "	\n"
                        + "	set theSlideShowSet to slide show settings of active presentation\n"
                        + "     try\n"
                        + "         set loop until stopped of theSlideShowSet to true\n"
                        + "         set show type of theSlideShowSet to slide show type presenter\n"
                        + "     on error errorMessage number errorNumber\n"
                        + "         return errorMessage\n"
                        + "     end try\n"
                        + "	run slide show theSlideShowSet\n"
                        + "end tell\n"
                        + "end if";
            case "focus":
                return "if application \"Microsoft PowerPoint\" is running then\n"
                        + "  tell application \"Microsoft PowerPoint\"\n"
                        + "	activate\n"
                        + "end tell\n"
                        + "else\n"
                        + "    return \"Not running\"\n"
                        + "end if";
            default:
                return null;
        }
    }

    /**
     * Send AppleScript signal.
     *
     * @param script signal to send.
     * @return result
     */
    private static String loadAppleScript(String script) {
        Runtime runtime = Runtime.getRuntime();
        String[] args = {"osascript", "-e", script};
        try {
            Process process = runtime.exec(args);
            return getMacResult(process);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Running AppleScript {0}", script);
        }
        return "";
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
        if (Utils.isMac()) {
            File installPath = new File("/Applications/Quelea/Quelea.jar");
            if (installPath.exists()) {
                Runtime runtime = Runtime.getRuntime();
                String[] args = {"open", installPath.getPath()};
                try {
                    Process process = runtime.exec(args);
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Could not return focus to Quelea main window");
                }
            }
        } else if (Utils.isWindows()) {
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
}
