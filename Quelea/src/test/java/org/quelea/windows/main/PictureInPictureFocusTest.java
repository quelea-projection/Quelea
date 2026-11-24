package org.quelea.windows.main;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.FxTestSupport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.quelea.testutil.FxTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/** Uses real mouse input and window focus, so it is opt-in on a desktop. */
@EnabledIfSystemProperty(named = "quelea.test.pipFocus", matches = "true")
class PictureInPictureFocusTest {
    @RepeatedTest(3)
    void doubleClickRestoresAndActivatesMainWindow(@TempDir Path temporaryDirectory, RepetitionInfo repetition) throws Exception {
        FxTestSupport.assumeAvailable();
        Stage[] main = new Stage[1];
        PictureInPictureWindow[] pip = new PictureInPictureWindow[1];
        Point2D originalMouse = onFx(() -> new Robot().getMousePosition());
        Process other = null;
        boolean maximized = repetition.getCurrentRepetition() == 2;
        try {
            onFx(() -> {
                main[0] = new Stage();
                StackPane content = new StackPane();
                main[0].setScene(new Scene(content, 400, 300));
                main[0].show();
                if (maximized) main[0].setMaximized(true);
                pip[0] = new PictureInPictureWindow(main[0], () -> content, () -> null, () -> { }, () -> null);
                return null;
            });
            if (maximized) {
                assumeTrue(waitFor(() -> main[0].isMaximized(), 5_000), "Window manager didn't maximise the main window");
            }
            onFx(() -> { pip[0].toggle(); return null; });
            assumeTrue(waitFor(() -> main[0].isIconified(), 5_000), "Window manager didn't minimise the main window");
            other = showOtherApplication(temporaryDirectory);
            Stage floating = onFx(() -> (Stage) Window.getWindows().stream()
                    .filter(w -> w instanceof Stage s && s != main[0]).findFirst().orElseThrow());
            onFx(() -> {
                Point2D centre = floating.getScene().getRoot().localToScreen(
                        floating.getScene().getWidth() / 2, floating.getScene().getHeight() / 2);
                new Robot().mouseMove(centre);
                return null;
            });
            Thread.sleep(700); // Let the hover expansion finish before clicking.
            onFx(() -> {
                Point2D centre = floating.getScene().getRoot().localToScreen(
                        floating.getScene().getWidth() / 2, floating.getScene().getHeight() / 2);
                Robot robot = new Robot();
                robot.mouseMove(centre);
                robot.mouseClick(MouseButton.PRIMARY);
                robot.mouseClick(MouseButton.PRIMARY);
                return null;
            });
            assertTrue(waitFor(() -> !pip[0].isShowing(), 5_000), "The double-click should close PiP");
            assertTrue(waitFor(() -> !main[0].isIconified() && main[0].isFocused(), 5_000),
                    () -> {
                        try {
                            return onFx(() -> "The restored main window should be focused above the other window: "
                                    + "main showing=" + main[0].isShowing() + ", iconified=" + main[0].isIconified()
                                    + ", focused=" + main[0].isFocused());
                        } catch (Exception ex) {
                            return "Couldn't inspect window state: " + ex;
                        }
                    });
            assertFalse(onFx(() -> main[0].isAlwaysOnTop()), "The main window must not stay always on top");
            if (maximized) {
                assertTrue(onFx(() -> main[0].isMaximized()), "The main window should stay maximized");
            }
        } finally {
            if (other != null) {
                other.destroy();
                if (!other.waitFor(5, TimeUnit.SECONDS)) {
                    other.destroyForcibly();
                    other.waitFor(5, TimeUnit.SECONDS);
                }
            }
            onFx(() -> {
                if (main[0] != null) main[0].hide();
                new Robot().mouseMove(originalMouse);
                return null;
            });
        }
    }

    /** Start a separate application and wait until the window manager focuses it. */
    static Process showOtherApplication(Path directory) throws Exception {
        Path source = directory.resolve("OtherWindow.java");
        Files.writeString(source, """
                import javax.swing.JFrame;
                import javax.swing.SwingUtilities;
                import java.awt.event.WindowAdapter;
                import java.awt.event.WindowEvent;
                public class OtherWindow {
                    public static void main(String[] args) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame window = new JFrame("PiP focus test - other application");
                            window.setSize(400, 300);
                            window.addWindowFocusListener(new WindowAdapter() {
                                public void windowGainedFocus(WindowEvent e) { System.out.println("FOCUSED"); }
                            });
                            window.setVisible(true);
                            window.toFront();
                            window.requestFocus();
                        });
                    }
                }
                """);
        String executable = org.quelea.services.utils.Utils.isWindows() ? "java.exe" : "java";
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                source.toString()).redirectErrorStream(true).start();
        CompletableFuture<Boolean> focused = CompletableFuture.supplyAsync(() -> {
            try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return output.lines().anyMatch("FOCUSED"::equals);
            } catch (Exception ex) {
                return false;
            }
        });
        try {
            assumeTrue(focused.get(15, TimeUnit.SECONDS), "The other application's window was not focused");
            return process;
        } catch (Throwable ex) {
            process.destroyForcibly();
            throw ex;
        }
    }
}
