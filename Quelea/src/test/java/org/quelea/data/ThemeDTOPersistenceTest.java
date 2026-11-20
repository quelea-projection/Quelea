package org.quelea.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;
import org.quelea.utils.ThemeUtils;

@DisplayName("Theme persistence")
public class ThemeDTOPersistenceTest {
    @TempDir Path directory;
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-theme-test").toString());
        }
    }

    @Test
    public void roundTripsColourBackgroundShadowAndTextSettings() {
        ThemeDTO original = theme("Sunday theme");
        ThemeDTO restored = ThemeDTO.fromString(original.asString(), Map.of());
        Assertions.assertEquals("Sunday theme", restored.getThemeName());
        Assertions.assertEquals(Color.BLUE, ((ColourBackground) restored.getBackground()).getColour());
        Assertions.assertEquals(Color.YELLOW, restored.getFontPaint());
        Assertions.assertEquals(Color.GREEN, restored.getTranslateFontPaint());
        Assertions.assertEquals(1, restored.getTextPosition());
        Assertions.assertEquals(2, restored.getTextAlignment());
        Assertions.assertEquals(3.0, restored.getShadow().getRadius());
        Assertions.assertEquals(1.5, restored.getShadow().getOffsetX());
        Assertions.assertTrue(restored.getShadow().getUse());
    }

    @Test
    public void themeNamesCanContainColons() {
        ThemeDTO original = theme("Communion: Easter");
        ThemeDTO restored = ThemeDTO.fromString(original.asString(), Map.of());
        Assertions.assertEquals("Communion: Easter", restored.getThemeName());
    }

    @Test
    public void themeNamesCanContainDollarSigns() {
        for (String name : new String[]{"Offering $5", "$", "$first", "last$", "one$$two",
                "Café 日本語 $5 & <friends>", "Offering $fontcolour:0xff0000ff"}) {
            ThemeDTO original = theme(name);
            String saved = original.asString();
            ThemeDTO restored = ThemeDTO.fromString(saved, Map.of());
            Assertions.assertEquals(name, restored.getThemeName());
            Assertions.assertEquals(original.getFontPaint(), restored.getFontPaint(), name);
            Assertions.assertEquals(original.getTextAlignment(), restored.getTextAlignment(), name);
            Assertions.assertEquals(name, ThemeDTO.fromString(original.getTheme().toString(), Map.of()).getThemeName());
            Assertions.assertTrue(saved.contains("$themename64:"));
            Assertions.assertEquals(name.substring(0, name.indexOf('$')), legacyThemeName(saved), name);
        }
    }

    @Test
    public void namesWithoutDollarSignsKeepTheExistingFormat() {
        String unnamed = theme("").asString();
        for (String name : new String[]{"Sunday theme", "Café 日本語", "Name %24 & <friends>", "Communion: Easter"}) {
            String saved = theme(name).asString();
            Assertions.assertEquals(unnamed.replace("$isFontBold:", "$themename:" + name + "$isFontBold:"), saved);
            Assertions.assertFalse(saved.contains("$themename64:"));
            Assertions.assertEquals(name, ThemeDTO.fromString(saved, Map.of()).getThemeName());
            if (!name.contains(":")) {
                Assertions.assertEquals(name, legacyThemeName(saved));
            }
        }
        Assertions.assertEquals("", legacyThemeName(unnamed));
    }

    @Test
    public void invalidEncodedNameFallsBackToLegacyName() {
        String legacy = theme("Fallback").asString();
        for (String invalid : new String[]{"%invalid%", "/w=="}) {
            ThemeDTO restored = ThemeDTO.fromString(legacy + "$themename64:" + invalid, Map.of());
            Assertions.assertEquals("Fallback", restored.getThemeName());
        }
    }

    @Test
    public void savedThemeFilePreservesDollarName() throws Exception {
        QueleaProperties previous = QueleaProperties.get();
        try {
            QueleaProperties.init(directory.toString());
            Path themes = Files.createDirectories(QueleaProperties.get().getQueleaUserHome().toPath().resolve("themes"));
            String name = "Café & friends $5";
            Files.writeString(themes.resolve("theme.th"), theme(name).getTheme().toString() + "\n", StandardCharsets.UTF_8);
            var loaded = ThemeUtils.getThemes();
            Assertions.assertEquals(1, loaded.size());
            Assertions.assertEquals(name, loaded.getFirst().getThemeName());
        } finally {
            var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, previous);
        }
    }

    // The theme-name handling used by releases before the colon parsing fix.
    private String legacyThemeName(String saved) {
        String name = "";
        for (String part : saved.split("\\$")) {
            String[] fields = part.split(":");
            if (fields.length > 1 && fields[0].equalsIgnoreCase("themename")) {
                name = fields[1];
            }
        }
        return name;
    }

    @Test
    public void videoBackgroundStretchSurvivesThemeSerialization() {
        SerializableFont font = new SerializableFont(Font.font("System", 24));
        ThemeDTO original = new ThemeDTO(font, Color.WHITE, font, Color.WHITE,
                new VideoBackground("background.mp4", true),
                new SerializableDropShadow(Color.BLACK, 0, 0, 2, 0, true),
                false, false, false, false, 0, 0);
        ThemeDTO restored = ThemeDTO.fromString(original.asString(), Map.of());
        Assertions.assertInstanceOf(VideoBackground.class, restored.getBackground());
        VideoBackground video = (VideoBackground) restored.getBackground();
        Assertions.assertEquals("background.mp4", video.getString());
        Assertions.assertTrue(video.getStretch());
        Assertions.assertFalse(original.asString().contains("$vidhue:"));
    }

    @Test
    public void legacyVideoHueIsIgnoredWhenReadingThemes() {
        SerializableFont font = new SerializableFont(Font.font("System", 24));
        ThemeDTO original = new ThemeDTO(font, Color.WHITE, font, Color.WHITE,
                new VideoBackground("background.mp4", true),
                new SerializableDropShadow(Color.BLACK, 0, 0, 2, 0, true),
                false, false, false, false, 0, 0);
        for (String hue : new String[]{"0.35", "not-a-number"}) {
            String legacy = original.asString().replace("$vidstretch:", "$vidhue:" + hue + "$vidstretch:");
            ThemeDTO restored = ThemeDTO.fromString(legacy, Map.of());
            VideoBackground video = Assertions.assertInstanceOf(VideoBackground.class, restored.getBackground());
            Assertions.assertEquals("background.mp4", video.getString());
            Assertions.assertTrue(video.getStretch());
            Assertions.assertFalse(restored.asString().contains("$vidhue:"));
        }
    }

    @Test
    public void videoBackgroundEqualityDistinguishesDisplaySettings() {
        VideoBackground first = new VideoBackground("background.mp4", false);
        Assertions.assertNotEquals(first, new VideoBackground("background.mp4", true));
    }

    @Test
    public void themeEqualityDistinguishesTextAlignment() {
        ThemeDTO first = theme("Same name");
        ThemeDTO second = theme("Same name");
        first.setTextAlignment(0);
        second.setTextAlignment(1);
        Assertions.assertNotEquals(first, second);
    }

    private ThemeDTO theme(String name) {
        SerializableFont font = new SerializableFont(Font.font("System", 24));
        ThemeDTO theme = new ThemeDTO(font, Color.YELLOW, font, Color.GREEN,
                new ColourBackground(Color.BLUE), new SerializableDropShadow(Color.BLACK, 1.5, 2, 3, 0.2, true),
                false, false, false, false, 1, 2);
        theme.setThemeName(name);
        return theme;
    }
}
