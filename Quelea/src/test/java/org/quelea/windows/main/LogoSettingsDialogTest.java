package org.quelea.windows.main;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.main.widgets.LogoPreview;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.FxTestSupport.onFx;

/**
 * The logo settings window (from the logo button) changes the logo settings
 * straight away. The window isn't shown; its controls are used directly.
 */
@DisplayName("Logo settings window")
public class LogoSettingsDialogTest {

    @TempDir
    Path home;

    @BeforeEach
    public void setUp() {
        FxTestSupport.assumeFxAvailable();
        QueleaProperties.init(home.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxTestSupport.useScratchHome();
    }

    @Test
    @DisplayName("Controls show the current settings and change them live")
    @SuppressWarnings("unchecked")
    public void changesApplyLive() throws Exception {
        QueleaProperties.get().setLogoImage("/church/logo.svg");
        QueleaProperties.get().setLogoSettings(new LogoSettings(0.5, LogoPosition.TOP_LEFT, 0.01, 0.02));
        AtomicInteger changes = new AtomicInteger();
        onFx(() -> {
            LogoSettingsDialog dialog = new LogoSettingsDialog(null, changes::incrementAndGet);
            TextField image = (TextField) dialog.getScene().lookup("#logo-image");
            Slider size = (Slider) dialog.getScene().lookup("#logo-size");
            ComboBox<LogoPosition> position = (ComboBox<LogoPosition>) dialog.getScene().lookup("#logo-position");
            Slider offsetX = (Slider) dialog.getScene().lookup("#logo-offset-x");
            Slider offsetY = (Slider) dialog.getScene().lookup("#logo-offset-y");

            assertEquals("/church/logo.svg", image.getText());
            assertEquals(50, size.getValue(), 1e-9);
            assertEquals(LogoPosition.TOP_LEFT, position.getValue());
            assertEquals(1, offsetX.getValue(), 1e-9);
            assertEquals(2, offsetY.getValue(), 1e-9);
            assertEquals(0, changes.get(), "filling in the controls isn't a change");

            size.setValue(25);
            position.setValue(LogoPosition.BOTTOM_RIGHT);
            offsetX.setValue(-3);
            offsetY.setValue(0);
            assertEquals(new LogoSettings(0.25, LogoPosition.BOTTOM_RIGHT, -0.03, 0), QueleaProperties.get().getLogoSettings());
            assertEquals(4, changes.get(), "every change updates the logo");

            // (What the preview shows is checked in previewFollowsSettings)
            LogoPreview preview = (LogoPreview) dialog.getScene().lookup(".logo-preview");
            assertTrue(preview != null, "the window has a preview");

            ((Button) dialog.getScene().lookup("#logo-reset")).fire();
            assertEquals(LogoSettings.DEFAULT, QueleaProperties.get().getLogoSettings());
            assertEquals(100, size.getValue(), 1e-9);
            assertTrue(changes.get() >= 5);
            return null;
        });
        // Saved, not just in memory
        QueleaProperties.init(home.toString());
        assertEquals(LogoSettings.DEFAULT, QueleaProperties.get().getLogoSettings());
        assertEquals("/church/logo.svg", QueleaProperties.get().getLogoImageLocation());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("The preview shows the chosen logo with the chosen settings")
    public void previewFollowsSettings() throws Exception {
        java.nio.file.Path svg = home.resolve("logo.svg");
        java.nio.file.Files.writeString(svg, "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\">"
                + "<rect width=\"10\" height=\"10\" fill=\"#ffffff\"/></svg>");
        QueleaProperties.get().setLogoImage(svg.toString());
        LogoPreview preview = onFx(() -> {
            LogoSettingsDialog dialog = new LogoSettingsDialog(null, () -> { });
            Slider size = (Slider) dialog.getScene().lookup("#logo-size");
            ComboBox<LogoPosition> position = (ComboBox<LogoPosition>) dialog.getScene().lookup("#logo-position");
            size.setValue(50);
            position.setValue(LogoPosition.TOP_RIGHT);
            LogoPreview p = (LogoPreview) dialog.getScene().lookup(".logo-preview");
            p.applyCss();
            p.layout();
            return p;
        });
        javafx.geometry.Rectangle2D bounds = onFx(() -> org.quelea.windows.main.widgets.LogoPreviewAccess.logoBounds(preview));
        double w = org.quelea.windows.main.widgets.LogoPreviewAccess.width(preview);
        double h = org.quelea.windows.main.widgets.LogoPreviewAccess.height(preview);
        assertEquals(Math.round(h / 2), bounds.getHeight(), 1, "half the height");
        assertEquals(w, bounds.getMaxX(), 1, "at the right");
        assertEquals(0, bounds.getMinY(), 1, "at the top");
        assertTrue(org.quelea.windows.main.widgets.LogoPreviewAccess.waitForImage(preview), "SVG rendered in the preview");
    }

    @Test
    @DisplayName("While a slider is dragged the logo moves, but it's only saved when let go")
    public void dragSavesOnRelease() throws Exception {
        onFx(() -> {
            LogoSettingsDialog dialog = new LogoSettingsDialog(null, () -> { });
            Slider size = (Slider) dialog.getScene().lookup("#logo-size");
            size.setValueChanging(true);
            size.setValue(30);
            assertEquals(0.3, QueleaProperties.get().getLogoSettings().size(), 1e-9);
            return null;
        });
        QueleaProperties.init(home.toString());
        assertEquals(1, QueleaProperties.get().getLogoSettings().size(), 1e-9, "not saved while dragging");
        onFx(() -> {
            LogoSettingsDialog dialog = new LogoSettingsDialog(null, () -> { });
            Slider size = (Slider) dialog.getScene().lookup("#logo-size");
            size.setValueChanging(true);
            size.setValue(30);
            size.setValueChanging(false);
            return null;
        });
        QueleaProperties.init(home.toString());
        assertEquals(0.3, QueleaProperties.get().getLogoSettings().size(), 1e-9, "saved once let go");
    }
}
