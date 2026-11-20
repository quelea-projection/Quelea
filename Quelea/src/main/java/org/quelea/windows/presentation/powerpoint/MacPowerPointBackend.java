/*
 * This file is part of Quelea, free projection software for churches.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import org.quelea.services.utils.LoggerUtils;

/**
 * Controls Microsoft PowerPoint on macOS through powerpoint.applescript.
 * <p>
 * Needs the Automation permission for PowerPoint. PowerPoint has no command
 * to jump to an arbitrary slide or to black the screen, so the script sends
 * the keys a presenter would press straight to the PowerPoint process, which
 * needs the Accessibility permission but leaves PowerPoint in the background
 * (Quelea keeps the focus). Without it a jump steps there with next/previous
 * (slides in between show for a moment) and black screen fails. Moving the
 * show to Quelea's projector screen also needs Accessibility.
 */
class MacPowerPointBackend implements PowerPointBackend {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    static final String BUNDLE_ID = "com.microsoft.Powerpoint";
    private static final long OPEN_TIMEOUT_MS = 60_000;
    private static final long EXPORT_TIMEOUT_MS = 180_000;
    private static final long COMMAND_TIMEOUT_MS = 10_000;

    /** Ends an answer when the show is not on Quelea's screen and may not be moved there. */
    static final String OFFSCREEN = "|OFFSCREEN";

    private final AppleScriptRunner runner;
    private final String returnPid;
    private File openFile;
    private PowerPointException warning;
    private boolean showOnScreen = true;

    MacPowerPointBackend(AppleScriptRunner runner, long returnPid) {
        this.runner = runner;
        this.returnPid = Long.toString(returnPid);
    }

    static MacPowerPointBackend create() throws IOException {
        PowerPointBridge bridge = null;
        try {
            bridge = PowerPointBridge.create();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not set up the PowerPoint bridge, using osascript", ex);
        }
        return new MacPowerPointBackend(new FallbackRunner(bridge, new OsascriptRunner()),
                ProcessHandle.current().pid());
    }

    /**
     * @return true if Microsoft PowerPoint is installed in one of the usual
     * places.
     */
    static boolean isInstalled() {
        Path[] candidates = {
            Paths.get("/Applications/Microsoft PowerPoint.app"),
            Paths.get(System.getProperty("user.home"), "Applications", "Microsoft PowerPoint.app")
        };
        for (Path p : candidates) {
            if (Files.isDirectory(p)) {
                return true;
            }
        }
        return false;
    }

    private SlideState command(long timeout, String... args) throws PowerPointException {
        String result = runner.run(timeout, args);
        if (result.endsWith(OFFSCREEN)) {
            result = result.substring(0, result.length() - OFFSCREEN.length());
            warning = new PowerPointException(PowerPointException.Kind.NOT_ACCESSIBLE,
                    PowerPointException.CODE_SHOW_OFF_SCREEN,
                    "PowerPoint started the show on the main screen and may not move it to the projector screen", null);
        }
        try {
            return SlideState.parse(result);
        } catch (IllegalArgumentException ex) {
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0,
                    "Unexpected answer to " + args[0] + ": " + result, ex);
        }
    }

    @Override
    public SlideState open(File file, Rectangle2D screen) throws PowerPointException {
        if (!isInstalled()) {
            throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, "Microsoft PowerPoint is not installed");
        }
        File wanted = file.getAbsoluteFile();
        if (openFile != null && !openFile.equals(wanted)) {
            try {
                close(); // Only one presentation of ours at a time.
            } catch (PowerPointException ex) {
                LOGGER.log(Level.INFO, "Could not close the previous presentation", ex);
            }
        }
        openFile = wanted;
        SlideState state = command(OPEN_TIMEOUT_MS, "openShow", openFile.getPath(), toRect(screen));
        showOnScreen = screen == null || checkScreen(screen);
        return state;
    }

    /**
     * @param area an area in screen coordinates.
     * @return "left,top,right,bottom" in whole points, or "" for none.
     */
    static String toRect(Rectangle2D area) {
        if (area == null || area.getWidth() <= 0 || area.getHeight() <= 0) {
            return "";
        }
        return Math.round(area.getMinX()) + "," + Math.round(area.getMinY()) + ","
                + Math.round(area.getMaxX()) + "," + Math.round(area.getMaxY());
    }

    /**
     * @param bounds "left,top,right,bottom" as showBounds returns it.
     * @return the area, or null if there is none.
     */
    static Rectangle2D parseRect(String bounds) {
        String[] p = bounds == null ? new String[0] : bounds.trim().split(",");
        if (p.length != 4) {
            return null;
        }
        try {
            double l = Double.parseDouble(p[0].trim());
            double t = Double.parseDouble(p[1].trim());
            double r = Double.parseDouble(p[2].trim());
            double b = Double.parseDouble(p[3].trim());
            return r > l && b > t ? new Rectangle2D(l, t, r - l, b - t) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * @return true if the (full screen) show covers the middle of Quelea's
     * projector area, i.e. it is on that screen, even when Quelea uses a
     * custom area smaller than the screen.
     */
    static boolean isOn(Rectangle2D show, Rectangle2D screen) {
        return show != null && show.contains(screen.getMinX() + screen.getWidth() / 2, screen.getMinY() + screen.getHeight() / 2);
    }

    /**
     * Log where the show ended up, so a show on the wrong screen can be told
     * apart from a show that did not start.
     */
    private boolean checkScreen(Rectangle2D screen) {
        try {
            Rectangle2D show = parseRect(runner.run(COMMAND_TIMEOUT_MS, "showBounds"));
            if (show == null) {
                return false;
            }
            if (isOn(show, screen)) {
                LOGGER.log(Level.INFO, "PowerPoint shows the presentation on Quelea''s projector screen ({0})", toRect(show));
                return true;
            }
            LOGGER.log(Level.WARNING, "PowerPoint shows the presentation at {0}, not on Quelea''s projector screen {1}",
                    new Object[]{toRect(show), toRect(screen)});
        } catch (PowerPointException ex) {
            LOGGER.log(Level.FINE, "Could not tell where the show is", ex);
        }
        return false;
    }

    @Override
    public boolean isShowOnScreen() {
        return showOnScreen;
    }

    @Override
    public SlideState close() throws PowerPointException {
        if (openFile == null) {
            return state();
        }
        File file = openFile;
        openFile = null;
        return command(COMMAND_TIMEOUT_MS, "closeShow", file.getPath());
    }

    @Override
    public SlideState next() throws PowerPointException {
        return command(COMMAND_TIMEOUT_MS, "nextSlide");
    }

    @Override
    public SlideState previous() throws PowerPointException {
        return command(COMMAND_TIMEOUT_MS, "previousSlide");
    }

    @Override
    public SlideState gotoSlide(int slide) throws PowerPointException {
        return command(COMMAND_TIMEOUT_MS, "gotoSlide", Integer.toString(slide), returnPid);
    }

    @Override
    public SlideState setBlack(boolean black) throws PowerPointException {
        return command(COMMAND_TIMEOUT_MS, "setBlack", Boolean.toString(black), returnPid);
    }

    @Override
    public SlideState state() throws PowerPointException {
        return command(COMMAND_TIMEOUT_MS, "state");
    }

    @Override
    public PowerPointException takeWarning() {
        PowerPointException w = warning;
        warning = null;
        return w;
    }

    @Override
    public void focus() throws PowerPointException {
        command(COMMAND_TIMEOUT_MS, "focus");
    }

    /**
     * Save the presentation as a PDF with PowerPoint (full fidelity, unlike
     * rendering the slides ourselves). PowerPoint is sandboxed and can only
     * write into its own container, so the PDF is written there and then moved
     * to {@code target}.
     *
     * @param file the presentation.
     * @param target where the PDF should end up.
     * @return the number of slides.
     */
    int exportPdf(File file, File target) throws PowerPointException {
        if (!isInstalled()) {
            throw new PowerPointException(PowerPointException.Kind.NOT_AVAILABLE, "Microsoft PowerPoint is not installed");
        }
        Path container = Paths.get(System.getProperty("user.home"), "Library", "Containers", BUNDLE_ID, "Data", "tmp", "quelea");
        Path pdf;
        try {
            Files.createDirectories(container);
            pdf = Files.createTempFile(container, "export", ".pdf");
            Files.delete(pdf); // PowerPoint must create it itself.
        } catch (IOException ex) {
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0, "Cannot write to PowerPoint's folder", ex);
        }
        try {
            String count = runner.run(EXPORT_TIMEOUT_MS, "exportPdf", file.getAbsolutePath(), pdf.toString());
            if (!Files.isRegularFile(pdf)) {
                throw new PowerPointException(PowerPointException.Kind.FAILED, "PowerPoint did not write " + pdf);
            }
            Files.createDirectories(target.toPath().getParent());
            Files.move(pdf, target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return Integer.parseInt(count.trim());
        } catch (IOException | NumberFormatException ex) {
            throw new PowerPointException(PowerPointException.Kind.FAILED, 0, "Could not export " + file, ex);
        } finally {
            try {
                Files.deleteIfExists(pdf);
            } catch (IOException ignored) {
                // Best effort.
            }
        }
    }

    @Override
    public boolean supportsPolling() {
        return runner instanceof FallbackRunner && ((FallbackRunner) runner).isFast();
    }

    @Override
    public void shutdown() {
        runner.close();
    }
}
