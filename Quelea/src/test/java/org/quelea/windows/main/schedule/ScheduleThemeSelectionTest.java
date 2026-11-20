package org.quelea.windows.main.schedule;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.ThemeDTO;
import org.quelea.services.utils.QueleaProperties;

class ScheduleThemeSelectionTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-theme-selection").toString());
        }
    }

    @Test
    void savedThemeSelectionFollowsItsFileAcrossEdits() {
        ThemeDTO selected = theme("Theme", 0);
        ThemeDTO edited = theme("Renamed", 1);
        selected.setFile(directory.resolve("chosen.th").toFile());
        edited.setFile(directory.resolve("sub/../chosen.th").toFile());
        Assertions.assertNotEquals(selected, edited);
        Assertions.assertTrue(ScheduleThemeNode.matchesSelection(selected, edited));
    }

    @Test
    void distinctSavedFilesDoNotBecomeOneSelectionEvenWhenThemesAreEqual() {
        ThemeDTO first = theme("Same", 0);
        ThemeDTO second = theme("Same", 0);
        first.setFile(directory.resolve("first.th").toFile());
        second.setFile(directory.resolve("second.th").toFile());
        Assertions.assertEquals(first, second);
        Assertions.assertFalse(ScheduleThemeNode.matchesSelection(first, second));
        Assertions.assertTrue(ScheduleThemeNode.matchesSelection(first, first));
    }

    @Test
    void unsavedThemesUseValueEqualityAndNullIsNotAStoredSelection() {
        ThemeDTO first = theme("Same", 0);
        Assertions.assertTrue(ScheduleThemeNode.matchesSelection(first, theme("Same", 0)));
        Assertions.assertFalse(ScheduleThemeNode.matchesSelection(first, theme("Same", 1)));
        Assertions.assertFalse(ScheduleThemeNode.matchesSelection(first, null));
        Assertions.assertFalse(ScheduleThemeNode.matchesSelection(null, first));
    }

    private ThemeDTO theme(String name, int alignment) {
        ThemeDTO d = ThemeDTO.DEFAULT_THEME;
        ThemeDTO theme = new ThemeDTO(d.getSerializableFont(), d.getFontPaint(),
                d.getTranslateSerializableFont(), d.getTranslateFontPaint(), d.getBackground(), d.getShadow(),
                d.isBold(), d.isItalic(), d.isTranslateBold(), d.isTranslateItalic(), d.getTextPosition(), alignment);
        theme.setThemeName(name);
        return theme;
    }
}
