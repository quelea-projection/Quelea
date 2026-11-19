package org.quelea.data;

import java.nio.file.Files;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableDropShadow;
import org.quelea.services.utils.SerializableFont;

@DisplayName("Theme persistence")
public class ThemeDTOPersistenceTest {
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
    @Disabled("Theme names containing the field separator are truncated")
    public void themeNamesCanContainDollarSigns() {
        // A user-supplied name should survive saving and reloading. The legacy
        // format uses '$' as a field separator without escaping; introduce a
        // backward-compatible encoding or versioned format before changing it.
        ThemeDTO original = theme("Offering $5");
        ThemeDTO restored = ThemeDTO.fromString(original.asString(), Map.of());
        Assertions.assertEquals("Offering $5", restored.getThemeName());
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
    @Disabled("Video background equality ignores stretch")
    public void videoBackgroundEqualityDistinguishesDisplaySettings() {
        // Theme-change detection uses background equality, so changing stretch
        // can be missed. Include it in equality and hashCode
        // together, after checking callers that depend on location-only equality.
        VideoBackground first = new VideoBackground("background.mp4", false);
        Assertions.assertNotEquals(first, new VideoBackground("background.mp4", true));
    }

    @Test
    @Disabled("Theme equality ignores text alignment")
    public void themeEqualityDistinguishesTextAlignment() {
        // Theme-change detection compares themes for equality. Ignoring layout
        // makes a visible change appear unchanged. Review all visual fields in
        // equals and hashCode together and distinguish callers that deliberately
        // want a looser default-theme comparison.
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
