package org.quelea.services.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.testutil.FxTestSupport;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quelea.services.utils.QueleaPropertyKeys.logoImageLocationKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoOffsetXKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoPositionKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoSizeKey;

@DisplayName("Logo properties")
public class LogoPropertiesTest {

    @TempDir
    Path home;

    @BeforeEach
    public void setUp() {
        QueleaProperties.init(home.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxTestSupport.useScratchHome();
    }

    @Test
    @DisplayName("An old properties file (no logo settings) keeps the old look")
    public void oldPropertiesFile() throws Exception {
        Files.writeString(QueleaProperties.get().getQueleaUserHome().toPath().resolve("quelea.properties"),
                logoImageLocationKey + "=/church/logo.png\n", StandardCharsets.UTF_8);
        QueleaProperties.init(home.toString());
        assertEquals(LogoSettings.DEFAULT, QueleaProperties.get().getLogoSettings());
        assertEquals("/church/logo.png", QueleaProperties.get().getLogoImageLocation());
        assertEquals(new File("/church/logo.png").toURI().toString(), QueleaProperties.get().getLogoImageURI());
    }

    @Test
    @DisplayName("No logo chosen means the default logo")
    public void defaultLogo() {
        assertEquals(QueleaProperties.DEFAULT_LOGO_LOCATION, QueleaProperties.get().getLogoImageLocation());
        QueleaProperties.get().setProperty(logoImageLocationKey, "  ");
        assertEquals(QueleaProperties.DEFAULT_LOGO_LOCATION, QueleaProperties.get().getLogoImageLocation());
    }

    @Test
    @DisplayName("Logo settings are saved and read back")
    public void roundTrip() {
        LogoSettings settings = new LogoSettings(0.3, LogoPosition.BOTTOM_LEFT, 0.02, -0.015);
        QueleaProperties.get().setLogoSettings(settings);
        assertEquals(settings, QueleaProperties.get().getLogoSettings());
        // And survive a restart
        QueleaProperties.init(home.toString());
        assertEquals(settings, QueleaProperties.get().getLogoSettings());
        assertEquals("BOTTOM_LEFT", QueleaProperties.get().getProperty(logoPositionKey));
    }

    @Test
    @DisplayName("Broken values fall back to the defaults")
    public void brokenValues() {
        QueleaProperties.get().setProperty(logoSizeKey, "huge");
        QueleaProperties.get().setProperty(logoPositionKey, "middle");
        QueleaProperties.get().setProperty(logoOffsetXKey, "12");
        LogoSettings settings = QueleaProperties.get().getLogoSettings();
        assertEquals(LogoSettings.DEFAULT.size(), settings.size());
        assertEquals(LogoPosition.CENTER, settings.position());
        assertEquals(LogoSettings.MAX_OFFSET, settings.offsetX());
    }
}
