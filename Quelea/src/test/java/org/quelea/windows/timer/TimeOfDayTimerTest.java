package org.quelea.windows.timer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.windows.main.widgets.Timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quelea.testutil.VideoTestSupport.assumeVideoAvailable;
import static org.quelea.testutil.VideoTestSupport.onFx;
import static org.quelea.testutil.VideoTestSupport.waitFor;

/**
 * Timers that count down to a time of day (#670): saved and loaded with their
 * time, and shown following the clock. Needs JavaFX; skipped where it isn't
 * available.
 */
@DisplayName("TimeOfDayTimer")
public class TimeOfDayTimerTest {

    @BeforeEach
    public void setUp() {
        assumeVideoAvailable();
    }

    private static TimerDisplayable parse(String xml) throws Exception {
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return TimerDisplayable.parseXML(doc.getFirstChild());
    }

    @Test
    @DisplayName("A time of day timer is saved and loaded with its time")
    public void timeOfDayRoundTrip() throws Exception {
        TimerDisplayable td = new TimerDisplayable("Service", LocalTime.of(9, 5), "Starts in ", "", ThemeDTO.DEFAULT_THEME);
        assertTrue(td.isCountdownToTime());
        assertTrue(td.getXML().contains("<time>09:05</time>"), td.getXML());
        TimerDisplayable loaded = parse(td.getXML());
        assertTrue(loaded.isCountdownToTime());
        assertEquals(LocalTime.of(9, 5), loaded.getTimeToFinish());
        assertEquals("Service", loaded.getName());
        assertEquals("Starts in ", loaded.getPretext());
        assertEquals("09:05", loaded.getTimeText());
    }

    @Test
    @DisplayName("Timers saved by older versions still load")
    public void olderTimers() throws Exception {
        String theme = ThemeDTO.DEFAULT_THEME.asXMLString();
        TimerDisplayable time = parse("<timer><name>Old</name><time>9:5</time><pretext></pretext><posttext></posttext><theme>"
                + theme + "</theme></timer>");
        assertEquals(LocalTime.of(9, 5), time.getTimeToFinish());
        TimerDisplayable duration = parse("<timer><name>Old</name><duration>300</duration><pretext></pretext><posttext></posttext><theme>"
                + theme + "</theme></timer>");
        assertFalse(duration.isCountdownToTime());
        assertEquals(300, duration.getSeconds());
    }

    @Test
    @DisplayName("The timer shows the time left until the target and follows the clock")
    public void followsTheClock() throws Exception {
        Assumptions.assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 50)), "Too close to midnight");
        LocalTime target = LocalTime.now().plusMinutes(2);
        Timer timer = onFx(() -> new Timer(target, "In ", "!"));
        String first = onFx(timer::getText);
        assertTrue(first.matches("In 0[12]:[0-5]\\d!"), first);
        onFx(timer::play);
        assertTrue(waitFor(() -> !timer.getText().equals(first), 3_000), "The countdown should move while playing");
        onFx(timer::stop);
    }

    @Test
    @DisplayName("A target more than an hour away shows hours, a passed one 00:00")
    public void hoursAndPassed() throws Exception {
        Assumptions.assumeTrue(LocalTime.now().isAfter(LocalTime.of(0, 10))
                && LocalTime.now().isBefore(LocalTime.of(21, 50)), "Too close to midnight");
        Timer later = onFx(() -> new Timer(LocalTime.now().plusMinutes(150), "", ""));
        assertTrue(onFx(later::getText).matches("2:[23]\\d:[0-5]\\d"), onFx(later::getText));
        Timer passed = onFx(() -> new Timer(LocalTime.now().minusMinutes(5), "", ""));
        assertEquals("00:00", onFx(passed::getText));
        onFx(later::stop);
        onFx(passed::stop);
    }
}
