package org.quelea.windows.options;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.BooleanField;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.QueleaPropertyKeys;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.options.customprefs.DefaultBibleSelector;

class PreferencesPersistenceTest {
    @TempDir Path directory;
    private QueleaProperties previousProperties;

    @BeforeEach
    void isolateProperties() {
        previousProperties = QueleaProperties.get();
        QueleaProperties.init(directory.toString());
    }

    @AfterEach
    void restoreProperties() throws Exception {
        var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previousProperties);
    }

    @Test
    void savingPreferencesPersistsVerseNumbersAndOtherSettings() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            var showNumbers = new SimpleBooleanProperty(true);
            var uniformSize = new SimpleBooleanProperty(false);
            var handler = new PreferenceStorageHandler(new InMemoryPreferences());
            var preferences = PreferencesFx.of(handler, Category.of("Bible",
                    Setting.of("Show verse numbers", showNumbers).customKey(QueleaPropertyKeys.showVerseNumbersKey),
                    Setting.of("Uniform font size", uniformSize).customKey(QueleaPropertyKeys.uniformFontSizeKey)));

            showNumbers.set(false);
            uniformSize.set(true);
            PreferencesDialog.savePreferences(preferences);
            QueleaProperties.init(directory.toString());
            Assertions.assertFalse(QueleaProperties.get().getShowVerseNumbers());
            Assertions.assertTrue(QueleaProperties.get().getUseUniformFontSize());

            showNumbers.set(true);
            uniformSize.set(false);
            PreferencesDialog.savePreferences(preferences);
            QueleaProperties.init(directory.toString());
            Assertions.assertTrue(QueleaProperties.get().getShowVerseNumbers());
            Assertions.assertFalse(QueleaProperties.get().getUseUniformFontSize());
            return null;
        });
    }

    @Test
    void pictureInPictureProjectionSettingDefaultsOnAndPersists() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            Assertions.assertTrue(QueleaProperties.get().getHideLiveViewDuringPictureInPicture(),
                    "existing installations should keep the current PiP behaviour");
            var group = OptionsDisplaySetupPanel.getPictureInPictureGroup();
            var field = (BooleanField) group.getSettings().get(0).getElement();
            var preferences = PreferencesFx.of(new PreferenceStorageHandler(new InMemoryPreferences()),
                    Category.of("Display", group));
            Assertions.assertTrue(field.getValue());
            field.valueProperty().set(false);
            PreferencesDialog.savePreferences(preferences);
            QueleaProperties.init(directory.toString());
            Assertions.assertFalse(QueleaProperties.get().getHideLiveViewDuringPictureInPicture());
            var reloaded = (BooleanField) OptionsDisplaySetupPanel.getPictureInPictureGroup()
                    .getSettings().get(0).getElement();
            Assertions.assertFalse(reloaded.getValue(), "the actual Display setting should reflect the saved value");
            field.valueProperty().set(true);
            PreferencesDialog.savePreferences(preferences);
            QueleaProperties.init(directory.toString());
            Assertions.assertTrue(QueleaProperties.get().getHideLiveViewDuringPictureInPicture());
            return null;
        });
    }

    @Test
    void bibleSelectorTracksReplacementItemsAndSelection() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            var field = Field.ofSingleSelectionType(FXCollections.observableArrayList("First", "Second"), 0);
            var control = new DefaultBibleSelector();
            control.setField(field);
            control.init();
            var row = (HBox) control.getNode().getChildren().get(0);
            var comboBox = (ComboBox<?>) row.getChildren().get(0);
            Assertions.assertEquals("First", comboBox.getSelectionModel().getSelectedItem());
            field.itemsProperty().set(FXCollections.observableArrayList("New", "Other"));
            field.select(1);
            Assertions.assertEquals(java.util.List.of("New", "Other"), comboBox.getItems());
            Assertions.assertEquals("Other", comboBox.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    /** Keeps PreferencesFX window state out of the user's real preferences store. */
    private static final class InMemoryPreferences extends AbstractPreferences {
        private final Map<String, String> values = new HashMap<>();

        InMemoryPreferences() { super(null, ""); }
        @Override protected void putSpi(String key, String value) { values.put(key, value); }
        @Override protected String getSpi(String key) { return values.get(key); }
        @Override protected void removeSpi(String key) { values.remove(key); }
        @Override protected void removeNodeSpi() { values.clear(); }
        @Override protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }
        @Override protected String[] childrenNamesSpi() { return new String[0]; }
        @Override protected AbstractPreferences childSpi(String name) { throw new UnsupportedOperationException(); }
        @Override protected void syncSpi() { }
        @Override protected void flushSpi() { }
    }
}
