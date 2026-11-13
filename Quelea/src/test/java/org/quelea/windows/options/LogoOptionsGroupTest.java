package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.dlsc.preferencesfx.util.StorageHandler;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.main.widgets.LogoImage;
import org.quelea.windows.main.widgets.LogoPreviewAccess;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.services.utils.QueleaPropertyKeys.logoImageLocationKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoOffsetXKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoOffsetYKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoPositionKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoSizeKey;
import static org.quelea.testutil.FxTestSupport.onFx;

/**
 * The logo settings on the Display tab of the options, used through the real
 * (rendered) controls and the real PreferencesFx save / discard, like the
 * options dialog does.
 */
@DisplayName("Logo options")
public class LogoOptionsGroupTest {

    @TempDir
    Path dir;

    private String logoA;
    private String logoB;

    @BeforeEach
    public void setUp() throws Exception {
        FxTestSupport.assumeFxAvailable();
        QueleaProperties.init(Files.createDirectory(dir.resolve("home")).toString());
        logoA = writeSvg("a.svg", 200, 100);
        logoB = writeSvg("b.svg", 100, 100);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxTestSupport.useScratchHome();
    }

    private String writeSvg(String name, int w, int h) throws Exception {
        Path file = dir.resolve(name);
        Files.writeString(file, "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + w + " " + h + "\">"
                + "<rect width=\"" + w + "\" height=\"" + h + "\" fill=\"#ffffff\"/></svg>", StandardCharsets.UTF_8);
        return file.toString();
    }

    /**
     * The options dialog's Display tab, rendered, as PreferencesDialog builds it.
     */
    private static class Options {
        final OptionsDisplaySetupPanel panel = new OptionsDisplaySetupPanel(new HashMap<>());
        final PreferencesFx prefs = PreferencesFx.of(new Storage(), panel.getDisplaySetupTab());
        final BorderPane pane = new BorderPane(prefs.getView().getCenter());

        Options() {
            new Scene(pane, 1000, 1200);
            layout();
        }

        LogoOptionsGroup logo() {
            return panel.getLogoGroup();
        }

        void layout() {
            pane.applyCss();
            pane.layout();
        }

        /** What PreferencesDialog does when it's shown. */
        void open() {
            logo().reload();
            layout();
        }

        /** What PreferencesDialog's OK button does. */
        void ok() {
            prefs.saveSettings();
            logo().apply();
        }

        /** What closing PreferencesDialog with the window's close button does. */
        void closeWithoutOk() {
            prefs.discardChanges();
        }

        Node control(int setting) {
            Field<?> field = (Field<?>) logo().getSettings().get(setting).getElement();
            return ((SimpleControl<?, ?>) field.getRenderer()).getNode();
        }

        TextField imageText() {
            return (TextField) control(0).lookup(".text-field");
        }

        Slider slider(int setting) {
            return (Slider) control(setting).lookup(".slider");
        }

        @SuppressWarnings("unchecked")
        ComboBox<String> position() {
            return (ComboBox<String>) control(2).lookup(".combo-box");
        }
    }

    @Test
    @DisplayName("A logo chosen from the logo button shows in the options and survives OK and closing")
    public void logoButtonChangesSurviveOptions() throws Exception {
        onFx(() -> {
            Options options = new Options();
            // Options were opened (and closed with OK) once before
            options.open();
            options.ok();

            // Right-click on the logo button: "Choose logo image..." and the
            // logo settings window (which gives any fraction from its sliders)
            QueleaProperties.get().setLogoImage(logoA);
            QueleaProperties.get().setLogoSettings(new LogoSettings(0.3337, LogoPosition.BOTTOM_LEFT, 0.0123, -0.0217));
            LogoImage.refreshAll();
            LogoSettings fromButton = QueleaProperties.get().getLogoSettings();

            // Opening the options shows it in the real controls
            options.open();
            assertEquals(logoA, options.imageText().getText());
            assertEquals(LogoPosition.BOTTOM_LEFT.getLabel(), options.position().getValue());
            assertEquals(0.3337, options.slider(1).getValue(), 0.01);

            // Closed with the window's close button: nothing changes (this
            // used to put the old image back)
            options.closeWithoutOk();
            assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
            assertEquals(fromButton, QueleaProperties.get().getLogoSettings());

            // Opened again and OK without touching the logo: nothing changes,
            // not even by slider rounding
            options.open();
            assertEquals(logoA, options.imageText().getText());
            options.ok();
            assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
            assertEquals(fromButton, QueleaProperties.get().getLogoSettings());
            return null;
        });
        // And on disk
        QueleaProperties.init(QueleaProperties.get().getQueleaUserHome().getParent());
        assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
    }

    @Test
    @DisplayName("Changing another logo setting in the options keeps the chosen image")
    public void changingPositionKeepsImage() throws Exception {
        onFx(() -> {
            Options options = new Options();
            QueleaProperties.get().setLogoImage(logoA);
            options.open();
            options.position().getSelectionModel().select(LogoPosition.TOP_RIGHT.getLabel());
            options.ok();
            assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
            assertEquals(LogoPosition.TOP_RIGHT, QueleaProperties.get().getLogoSettings().position());
            assertEquals(LogoSettings.DEFAULT.size(), QueleaProperties.get().getLogoSettings().size());
            return null;
        });
    }

    @Test
    @DisplayName("Edits in the controls show in the preview straight away, and are saved on OK only")
    public void editsPreviewAndSave() throws Exception {
        Options options = onFx(() -> {
            Options o = new Options();
            QueleaProperties.get().setLogoImage(logoA);
            o.open();
            o.imageText().setText(logoB); // As the Browse button does
            o.position().getSelectionModel().select(LogoPosition.BOTTOM_RIGHT.getLabel());
            o.slider(1).setValue(0.25);
            o.layout();
            return o;
        });
        // Preview: a square logo, a quarter of the screen high, bottom right
        Rectangle2D bounds = onFx(() -> {
            options.layout();
            return LogoPreviewAccess.logoBounds(options.logo().getPreview());
        });
        double w = LogoPreviewAccess.width(options.logo().getPreview());
        double h = LogoPreviewAccess.height(options.logo().getPreview());
        assertEquals(Math.round(h * 0.25), bounds.getWidth(), 1, "square, a quarter of the height");
        assertEquals(w, bounds.getMaxX(), 1, "at the right");
        assertEquals(h, bounds.getMaxY(), 1, "at the bottom");
        assertTrue(LogoPreviewAccess.waitForImage(options.logo().getPreview()), "the SVG should be rendered in the preview");
        // Not saved yet
        assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
        assertEquals(LogoSettings.DEFAULT, QueleaProperties.get().getLogoSettings());

        onFx(() -> {
            options.ok();
            return null;
        });
        assertEquals(logoB, QueleaProperties.get().getLogoImageLocation());
        assertEquals(new LogoSettings(0.25, LogoPosition.BOTTOM_RIGHT, 0, 0), QueleaProperties.get().getLogoSettings());
    }

    @Test
    @DisplayName("Closing without OK throws away edits")
    public void closeDiscardsEdits() throws Exception {
        onFx(() -> {
            Options options = new Options();
            QueleaProperties.get().setLogoImage(logoA);
            options.open();
            options.imageText().setText(logoB);
            options.position().getSelectionModel().select(LogoPosition.TOP_LEFT.getLabel());
            options.closeWithoutOk();
            assertEquals(logoA, QueleaProperties.get().getLogoImageLocation());
            assertEquals(LogoSettings.DEFAULT, QueleaProperties.get().getLogoSettings());
            // Next time it shows what's saved, not the thrown away edits
            options.open();
            assertEquals(logoA, options.imageText().getText());
            assertEquals(LogoPosition.CENTER.getLabel(), options.position().getValue());
            return null;
        });
    }

    @Test
    @DisplayName("Only the logo keys are handled by the logo group")
    public void logoKeys() {
        for (String key : List.of(logoImageLocationKey, logoSizeKey, logoPositionKey, logoOffsetXKey, logoOffsetYKey)) {
            assertTrue(LogoOptionsGroup.isLogoKey(key), key);
        }
        assertFalse(LogoOptionsGroup.isLogoKey("logo.fade.duration"));
        assertFalse(LogoOptionsGroup.isLogoKey("small.song.text.v.position"));
    }

    @Test
    @DisplayName("Loading gives the defaults when nothing is set")
    public void loadDefaults() {
        assertEquals(QueleaProperties.DEFAULT_LOGO_LOCATION, LogoOptionsGroup.loadValue(logoImageLocationKey));
        assertEquals(1.0, LogoOptionsGroup.loadValue(logoSizeKey));
        assertEquals(LogoPosition.CENTER.getLabel(), LogoOptionsGroup.loadValue(logoPositionKey));
        assertEquals(0.0, LogoOptionsGroup.loadValue(logoOffsetXKey));
        assertEquals(0.0, LogoOptionsGroup.loadValue(logoOffsetYKey));
    }

    /**
     * Does what {@link PreferenceStorageHandler} does for the logo keys (it
     * can't be used here: it would touch the user's real Java preferences),
     * and nothing for the others.
     */
    private static class Storage implements StorageHandler {
        @Override
        public Object loadObject(String breadcrumb, Object defaultObject) {
            return LogoOptionsGroup.isLogoKey(breadcrumb) ? LogoOptionsGroup.loadValue(breadcrumb) : defaultObject;
        }

        @Override
        public void saveObject(String breadcrumb, Object object) {
            // Logo keys: saved by LogoOptionsGroup.apply(), never here
        }

        @Override
        public <T> T loadObject(String breadcrumb, Class<T> type, T defaultObject) {
            return defaultObject;
        }

        @Override
        public ObservableList loadObservableList(String breadcrumb, ObservableList defaultObservableList) {
            return defaultObservableList;
        }

        @Override
        public <T> ObservableList<T> loadObservableList(String breadcrumb, Class<T> type, ObservableList<T> defaultObservableList) {
            return defaultObservableList;
        }

        @Override
        public void saveSelectedCategory(String breadcrumb) {
        }

        @Override
        public String loadSelectedCategory() {
            return null;
        }

        @Override
        public void saveDividerPosition(double dividerPosition) {
        }

        @Override
        public double loadDividerPosition() {
            return 0.2;
        }

        @Override
        public void saveWindowWidth(double windowWidth) {
        }

        @Override
        public double loadWindowWidth() {
            return 1000;
        }

        @Override
        public void saveWindowHeight(double windowHeight) {
        }

        @Override
        public double loadWindowHeight() {
            return 700;
        }

        @Override
        public void saveWindowPosX(double windowPosX) {
        }

        @Override
        public double loadWindowPosX() {
            return 0;
        }

        @Override
        public void saveWindowPosY(double windowPosY) {
        }

        @Override
        public double loadWindowPosY() {
            return 0;
        }

        @Override
        public boolean clearPreferences() {
            return true;
        }
    }
}
