package org.quelea.windows.timer;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Saved countdown timers")
public class TimerIOTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-timer-io-test").toString());
        }
    }

    @Test
    public void roundTripsDurationAndUnicodeText() throws Exception {
        Path path = directory.resolve("timer.xml");
        TimerDisplayable original = new TimerDisplayable("Tea & caf\u00e9", 125, "Ready <soon>", "Go!", ThemeDTO.DEFAULT_THEME);
        TimerIO.timerToFile(original, path.toFile());
        TimerDisplayable restored = TimerIO.timerFromFile(path.toFile());
        Assertions.assertNotNull(restored);
        Assertions.assertEquals("Tea & caf\u00e9", restored.getName());
        Assertions.assertEquals(125, restored.getSeconds());
        Assertions.assertEquals("Ready <soon>", restored.getPretext());
        Assertions.assertEquals("Go!", restored.getPosttext());
    }

    @Test
    public void returnsNullForMissingAndMalformedFiles() throws Exception {
        Assertions.assertNull(TimerIO.timerFromFile(directory.resolve("missing.xml").toFile()));
        Path broken = Files.writeString(directory.resolve("broken.xml"), "<timer>");
        Assertions.assertNull(TimerIO.timerFromFile(broken.toFile()));
    }
}
