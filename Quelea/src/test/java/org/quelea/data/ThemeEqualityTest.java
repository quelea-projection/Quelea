package org.quelea.data;

import java.nio.file.Files;
import java.util.HashSet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;

class ThemeEqualityTest {
    @BeforeAll
    static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-theme-equality").toString());
        }
    }

    @Test
    void videoBackgroundEqualityIncludesLocationAndStretch() {
        VideoBackground first = new VideoBackground("video.mp4", false);
        VideoBackground equal = new VideoBackground("video.mp4", false);
        assertEqualContract(first, equal);
        Assertions.assertNotEquals(first, new VideoBackground("video.mp4", true));
        Assertions.assertNotEquals(first, new VideoBackground("other.mp4", false));
        Assertions.assertFalse(first.equals(null));
    }

    @Test
    void imageBackgroundEqualityIgnoresTheImageCache() throws Exception {
        ImageBackground first = new ImageBackground("missing-background.png");
        ImageBackground equal = new ImageBackground("missing-background.png");
        assertEqualContract(first, equal);
        Assertions.assertNotEquals(first, new ImageBackground("other-background.png"));
        var cache = ImageBackground.class.getDeclaredField("originalImage");
        cache.setAccessible(true);
        int hash = first.hashCode();
        ((java.lang.ref.Reference<?>) cache.get(first)).clear();
        first.getImage();
        Assertions.assertEquals(hash, first.hashCode());
        assertEqualContract(first, equal);
    }

    @Test
    void themeEqualityIncludesDisplayAndSavedPropertiesButNotFileLocation() {
        ThemeDTO first = theme(Color.GREEN, new SerializableFont(Font.font("System", 24)), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true), 1, 0);
        ThemeDTO equal = theme(Color.GREEN, new SerializableFont(Font.font("System", 24)), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true), 1, 0);
        first.setThemeName("Theme");
        equal.setThemeName("Theme");
        first.setFile(new java.io.File("one.th"));
        equal.setFile(new java.io.File("two.th"));
        assertEqualContract(first, equal);

        ThemeDTO translationColor = theme(Color.RED, equal.getTranslateSerializableFont(), false, equal.getShadow(), 1, 0);
        ThemeDTO translationFont = theme(Color.GREEN, new SerializableFont(Font.font("System", 31)), false, equal.getShadow(), 1, 0);
        ThemeDTO bold = theme(Color.GREEN, equal.getTranslateSerializableFont(), true, equal.getShadow(), 1, 0);
        ThemeDTO shadow = theme(Color.GREEN, equal.getTranslateSerializableFont(), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 4, 0.2, true), 1, 0);
        ThemeDTO position = theme(Color.GREEN, equal.getTranslateSerializableFont(), false, equal.getShadow(), 2, 0);
        ThemeDTO alignment = theme(Color.GREEN, equal.getTranslateSerializableFont(), false, equal.getShadow(), 1, 1);
        for (ThemeDTO variant : new ThemeDTO[]{translationColor, translationFont, bold, shadow, position, alignment}) {
            variant.setThemeName("Theme");
            Assertions.assertNotEquals(first, variant);
            Assertions.assertNotEquals(variant, first);
        }
        equal.setThemeName("Different name");
        Assertions.assertNotEquals(first, equal);
    }

    @Test
    void comparisonIgnoringNameAndFontSizeStillChecksVisualDifferences() {
        ThemeDTO first = theme(Color.GREEN, new SerializableFont(Font.font("System", 24)), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true), 1, 0);
        ThemeDTO second = new ThemeDTO(new SerializableFont(Font.font("System", 14)), first.getFontPaint(),
                new SerializableFont(Font.font("System", 14)), first.getTranslateFontPaint(),
                first.getBackground(), new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true),
                false, false, false, false, 1, 0);
        second.setThemeName("Other");
        Assertions.assertNotEquals(first, second);
        Assertions.assertTrue(first.equalsIgnoreNameAndFontSize(second));
        Assertions.assertTrue(second.equalsIgnoreNameAndFontSize(first));
        second.setTextAlignment(1);
        Assertions.assertFalse(first.equalsIgnoreNameAndFontSize(second));
    }

    @Test
    void savedThemeRoundTripKeepsValueEquality() {
        ThemeDTO d = ThemeDTO.DEFAULT_THEME;
        ThemeDTO original = new ThemeDTO(d.getSerializableFont(), d.getFontPaint(),
                d.getTranslateSerializableFont(), d.getTranslateFontPaint(), new VideoBackground("video.mp4", true),
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true),
                false, false, false, true, 2, 1);
        original.setThemeName("Saved theme");
        ThemeDTO restored = ThemeDTO.fromString(original.asString(), java.util.Map.of());
        assertEqualContract(original, restored);
        ThemeDTO differentStretch = new ThemeDTO(d.getSerializableFont(), d.getFontPaint(),
                d.getTranslateSerializableFont(), d.getTranslateFontPaint(), new VideoBackground("video.mp4", false),
                original.getShadow(), false, false, false, true, 2, 1);
        differentStretch.setThemeName("Saved theme");
        Assertions.assertNotEquals(original, differentStretch);
    }

    @Test
    void shadowEqualityChecksEverySavedProperty() {
        SerializableDropShadow first = new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true);
        assertEqualContract(first, new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true));
        for (SerializableDropShadow different : new SerializableDropShadow[]{
                new SerializableDropShadow(Color.RED, 1, 2, 3, 0.2, true),
                new SerializableDropShadow(Color.BLACK, 2, 2, 3, 0.2, true),
                new SerializableDropShadow(Color.BLACK, 1, 3, 3, 0.2, true),
                new SerializableDropShadow(Color.BLACK, 1, 2, 4, 0.2, true),
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.3, true),
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, false)}) {
            Assertions.assertNotEquals(first, different);
            Assertions.assertNotEquals(different, first);
        }
    }

    @Test
    void everyFontStyleFlagAndMainTextPropertyAffectsThemeEquality() {
        ThemeDTO first = theme(Color.GREEN, new SerializableFont(Font.font("System", 24)), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true), 1, 0);
        for (int changedFlag = 0; changedFlag < 4; changedFlag++) {
            ThemeDTO different = new ThemeDTO(first.getSerializableFont(), first.getFontPaint(),
                    first.getTranslateSerializableFont(), first.getTranslateFontPaint(), first.getBackground(), first.getShadow(),
                    changedFlag == 0, changedFlag == 1, changedFlag == 2, changedFlag == 3, 1, 0);
            Assertions.assertNotEquals(first, different, "Style flag " + changedFlag);
            Assertions.assertFalse(first.equalsIgnoreNameAndFontSize(different), "Style flag " + changedFlag);
        }
        ThemeDTO differentMainColor = new ThemeDTO(first.getSerializableFont(), Color.RED,
                first.getTranslateSerializableFont(), first.getTranslateFontPaint(), first.getBackground(), first.getShadow(),
                false, false, false, false, 1, 0);
        ThemeDTO differentMainSize = new ThemeDTO(new SerializableFont(Font.font("System", 32)), first.getFontPaint(),
                first.getTranslateSerializableFont(), first.getTranslateFontPaint(), first.getBackground(), first.getShadow(),
                false, false, false, false, 1, 0);
        Assertions.assertNotEquals(first, differentMainColor);
        Assertions.assertNotEquals(first, differentMainSize);
        Assertions.assertTrue(first.equalsIgnoreNameAndFontSize(differentMainSize));
    }

    @Test
    void mutableNameAndLayoutDoNotChangeTheThemeHash() {
        ThemeDTO value = theme(Color.GREEN, new SerializableFont(Font.font("System", 24)), false,
                new SerializableDropShadow(Color.BLACK, 1, 2, 3, 0.2, true), 1, 0);
        var values = new HashSet<>(java.util.List.of(value));
        int originalHash = value.hashCode();
        value.setThemeName("Renamed");
        value.setTextAlignment(1);
        value.setTextPosition(2);
        Assertions.assertEquals(originalHash, value.hashCode());
        Assertions.assertTrue(values.contains(value));
    }

    private ThemeDTO theme(Color translation, SerializableFont translationFont, boolean bold,
            SerializableDropShadow shadow, int position, int alignment) {
        return new ThemeDTO(new SerializableFont(Font.font("System", 24)), Color.WHITE,
                translationFont, translation, new VideoBackground("video.mp4", false), shadow,
                bold, false, false, false, position, alignment);
    }

    private <T> void assertEqualContract(T first, T second) {
        Assertions.assertEquals(first, first);
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(second, first);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
        Assertions.assertTrue(new HashSet<>(java.util.List.of(first)).contains(second));
    }
}
