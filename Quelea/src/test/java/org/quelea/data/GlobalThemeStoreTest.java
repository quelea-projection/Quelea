package org.quelea.data;

import java.nio.file.Path;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;

class GlobalThemeStoreTest {
    @TempDir Path home;
    private QueleaProperties previous;
    private GlobalThemeStore store;
    private ThemeDTO global;

    @BeforeEach
    void setUp() {
        previous = QueleaProperties.get();
        QueleaProperties.init(home.toString());
        QueleaProperties.get().setItemThemeOverride(true);
        store = new GlobalThemeStore();
        ThemeDTO defaults = ThemeDTO.DEFAULT_THEME;
        global = copy(defaults, new ColourBackground(Color.BLUE), defaults.getShadow(),
                defaults.getTextPosition(), defaults.getTextAlignment());
        store.setSongThemeOverride(global);
        store.setBibleThemeOverride(global);
    }

    @AfterEach
    void restore() throws Exception {
        var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previous);
    }

    @Test
    void globalThemesApplyToBuiltInSongAndBibleDefaultsIncludingReloadedThemes() {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        BiblePassage bible = new BiblePassage("Test Bible", "John 1:1", new BibleVerse[0], false);
        for (TextDisplayable item : new TextDisplayable[]{song, bible}) {
            ThemeDTO itemDefault = item.getTheme();
            Assertions.assertSame(global, resolve(item, itemDefault));
            Assertions.assertSame(global, resolve(item, ThemeDTO.fromString(itemDefault.asString(), Map.of())));
            Assertions.assertSame(global, resolve(item, ThemeDTO.getDTO(itemDefault.getTheme())));
            Assertions.assertSame(global, resolve(item, ThemeDTO.DEFAULT_THEME));
        }
    }

    @Test
    void defaultRecognitionIgnoresThemeNameAndFontSizes() {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        ThemeDTO itemDefault = song.getTheme();
        var smaller = new SerializableFont(Font.font(itemDefault.getFont().getFamily(), 13));
        ThemeDTO renamed = new ThemeDTO(smaller, itemDefault.getFontPaint(), smaller,
                itemDefault.getTranslateFontPaint(), itemDefault.getBackground(), itemDefault.getShadow(),
                itemDefault.isBold(), itemDefault.isItalic(), itemDefault.isTranslateBold(), itemDefault.isTranslateItalic(),
                itemDefault.getTextPosition(), itemDefault.getTextAlignment());
        renamed.setThemeName("Renamed default");
        Assertions.assertSame(global, resolve(song, renamed));
    }

    @Test
    void customAlignmentAndShadowTakePrecedenceWhenItemOverrideIsEnabled() {
        SongDisplayable song = new SongDisplayable("Song", "Author");
        ThemeDTO defaults = song.getTheme();
        ThemeDTO aligned = copy(defaults, defaults.getBackground(), defaults.getShadow(),
                defaults.getTextPosition(), 1);
        ThemeDTO shadow = copy(defaults, defaults.getBackground(),
                new SerializableDropShadow(Color.RED, 1, 2, 3, 0.2, true),
                defaults.getTextPosition(), defaults.getTextAlignment());
        Assertions.assertSame(aligned, resolve(song, aligned));
        Assertions.assertSame(shadow, resolve(song, shadow));
        QueleaProperties.get().setItemThemeOverride(false);
        Assertions.assertSame(global, resolve(song, aligned));
    }

    @Test
    void bibleCustomLayoutAndTranslationAreNotMistakenForDefaults() {
        BiblePassage bible = new BiblePassage("Test Bible", "John 1:1", new BibleVerse[0], false);
        ThemeDTO defaults = bible.getTheme();
        ThemeDTO aligned = copy(defaults, defaults.getBackground(), defaults.getShadow(),
                defaults.getTextPosition(), 0);
        ThemeDTO translation = new ThemeDTO(defaults.getSerializableFont(), defaults.getFontPaint(),
                defaults.getTranslateSerializableFont(), Color.RED, defaults.getBackground(), defaults.getShadow(),
                defaults.isBold(), defaults.isItalic(), defaults.isTranslateBold(), defaults.isTranslateItalic(),
                defaults.getTextPosition(), defaults.getTextAlignment());
        Assertions.assertSame(aligned, resolve(bible, aligned));
        Assertions.assertSame(translation, resolve(bible, translation));
        QueleaProperties.get().setItemThemeOverride(false);
        Assertions.assertSame(global, resolve(bible, aligned));
        store.setBibleThemeOverride(null);
        Assertions.assertSame(aligned, resolve(bible, aligned));
    }

    private ThemeDTO resolve(TextDisplayable item, ThemeDTO theme) {
        return store.getTheme(item, new TextSection("", new String[]{"Words"}, null, false, theme));
    }

    private ThemeDTO copy(ThemeDTO original, Background background, SerializableDropShadow shadow, int position, int alignment) {
        ThemeDTO copy = new ThemeDTO(original.getSerializableFont(), original.getFontPaint(),
                original.getTranslateSerializableFont(), original.getTranslateFontPaint(), background, shadow,
                original.isBold(), original.isItalic(), original.isTranslateBold(), original.isTranslateItalic(), position, alignment);
        copy.setThemeName(original.getThemeName());
        return copy;
    }
}
