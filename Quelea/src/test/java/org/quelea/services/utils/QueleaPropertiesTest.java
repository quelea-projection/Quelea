package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.List;
import java.nio.file.Path;

@DisplayName("QueleaPropertiesTest")
public class QueleaPropertiesTest {
    @TempDir Path directory;
    private QueleaProperties previousProperties;

    @BeforeEach
    public void isolateProperties() {
        previousProperties = QueleaProperties.get();
        QueleaProperties.init(directory.toString());
    }

    @AfterEach
    public void restoreProperties() throws Exception {
        var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previousProperties);
    }

    @Test
    @DisplayName("Test If Properties File Is Sorted")
    // check whether properties file is sorted
    public void testIfPropertiesFileIsSorted() throws FileNotFoundException {
        // trigger a properties write
        QueleaProperties.get().setCheckUpdate(true);
        // read the properties file
        ArrayList<String> propertyKeys = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(QueleaProperties.get().getQueleaUserHome(), "quelea.properties"))) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(!line.isEmpty() && line.charAt(0) != '#') {
                    propertyKeys.add(line.split("=")[0]);
                }
            }
        }
        // make a copy of key properties
        ArrayList<String> propertyKeysSorted = new ArrayList<>(propertyKeys);
        // sort the copy
        Collections.sort(propertyKeysSorted);
        // If both are equal then keys were already in sorted order
        Assertions.assertEquals(propertyKeys, propertyKeysSorted);
    }

    @Test
    @DisplayName("Settings persist when the properties file is reloaded")
    public void settingsSurviveReload() {
        QueleaProperties properties = QueleaProperties.get();
        properties.setRemoteControlPort(12345);
        properties.setRemoteControlPassword("test password & symbols");
        properties.setChosenFonts(List.of("Arial", "Noto Sans"));
        QueleaProperties.init(directory.toString());
        Assertions.assertEquals(12345, QueleaProperties.get().getRemoteControlPort());
        Assertions.assertEquals("test password & symbols", QueleaProperties.get().getRemoteControlPassword());
        Assertions.assertEquals(List.of("Arial", "Noto Sans"), QueleaProperties.get().getChosenFonts());
    }

    @Test
    @Disabled("Changing verse-number visibility does not write the properties file")
    public void verseNumberSettingSurvivesReload() {
        // Users expect this option to survive a restart. The setter only changes
        // the in-memory Properties object. Establish a consistent persistence
        // boundary for settings changes, including this setter, before reloading.
        QueleaProperties.get().setShowVerseNumbers(false);
        QueleaProperties.init(directory.toString());
        Assertions.assertFalse(QueleaProperties.get().getShowVerseNumbers());
    }

    @Test
    @DisplayName("Older dark-theme settings migrate when no new theme is set")
    public void legacyDarkThemeFallback() {
        QueleaProperties properties = QueleaProperties.get();
        properties.setProperty(QueleaPropertyKeys.darkThemeKey, "true");
        Assertions.assertEquals("dark", properties.getUiTheme());
        properties.setUiTheme("light");
        Assertions.assertEquals("light", properties.getUiTheme());
        Assertions.assertEquals("false", properties.getProperty(QueleaPropertyKeys.darkThemeKey));
    }

    @Test
    @DisplayName("Invalid stored remote-control ports use the default")
    public void invalidPortFallsBack() {
        QueleaProperties.get().setProperty(QueleaPropertyKeys.remoteControlPortKey, "not-a-number");
        Assertions.assertEquals(1112, QueleaProperties.get().getRemoteControlPort());
    }
}
