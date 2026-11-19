package org.quelea.windows.timer;

import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CountdownTimesTest")
public class CountdownTimesTest {

    @Test
    @DisplayName("Durations are read in the forms the dialog accepts")
    public void durations() {
        Assertions.assertEquals(300, CountdownTimes.parseDuration("5:00"));
        Assertions.assertEquals(330, CountdownTimes.parseDuration("5.30"));
        Assertions.assertEquals(330, CountdownTimes.parseDuration("5,30"));
        Assertions.assertEquals(330, CountdownTimes.parseDuration("5m30s"));
        Assertions.assertEquals(300, CountdownTimes.parseDuration("5m"));
        Assertions.assertEquals(3600, CountdownTimes.parseDuration("60:00"));
        for (String bad : new String[]{"", "5", "5:60", "60:01", "-1:00", "a:b", "1:2:3", "9:30am", null}) {
            Assertions.assertEquals(-1, CountdownTimes.parseDuration(bad), String.valueOf(bad));
        }
    }

    @Test
    @DisplayName("Times of day are read in 24 and 12 hour form")
    public void timesOfDay() {
        Assertions.assertEquals(LocalTime.of(9, 30), CountdownTimes.parseTimeOfDay("09:30"));
        Assertions.assertEquals(LocalTime.of(9, 30), CountdownTimes.parseTimeOfDay("9.30"));
        Assertions.assertEquals(LocalTime.of(21, 30), CountdownTimes.parseTimeOfDay("21:30"));
        Assertions.assertEquals(LocalTime.of(9, 30), CountdownTimes.parseTimeOfDay("9:30am"));
        Assertions.assertEquals(LocalTime.of(21, 30), CountdownTimes.parseTimeOfDay("9:30 PM"));
        Assertions.assertEquals(LocalTime.of(21, 30), CountdownTimes.parseTimeOfDay("9:30 p.m."));
        Assertions.assertEquals(LocalTime.of(0, 15), CountdownTimes.parseTimeOfDay("12:15am"));
        Assertions.assertEquals(LocalTime.of(12, 15), CountdownTimes.parseTimeOfDay("12:15pm"));
        Assertions.assertEquals(LocalTime.of(18, 0), CountdownTimes.parseTimeOfDay("6pm"));
        // Saved by older versions without padding
        Assertions.assertEquals(LocalTime.of(9, 5), CountdownTimes.parseTimeOfDay("9:5"));
        for (String bad : new String[]{"", "9", "24:00", "9:60", "13:00pm", "0:30am", "nine", null}) {
            Assertions.assertNull(CountdownTimes.parseTimeOfDay(bad), String.valueOf(bad));
        }
    }

    @Test
    @DisplayName("A time of day is saved and read back unchanged")
    public void timeOfDayRoundTrip() {
        LocalTime time = LocalTime.of(7, 5);
        Assertions.assertEquals("07:05", CountdownTimes.formatTimeOfDay(time));
        Assertions.assertEquals(time, CountdownTimes.parseTimeOfDay(CountdownTimes.formatTimeOfDay(time)));
    }

    @Test
    @DisplayName("The seconds left run to zero at the target time and stay there")
    public void secondsUntil() {
        LocalTime target = LocalTime.of(9, 30);
        Assertions.assertEquals(1800, CountdownTimes.secondsUntil(target, LocalTime.of(9, 0)));
        Assertions.assertEquals(1, CountdownTimes.secondsUntil(target, LocalTime.of(9, 29, 59, 500_000_000)));
        Assertions.assertEquals(0, CountdownTimes.secondsUntil(target, target));
        Assertions.assertEquals(0, CountdownTimes.secondsUntil(target, LocalTime.of(10, 0)));
        Assertions.assertEquals(9000, CountdownTimes.secondsUntil(target, LocalTime.of(7, 0)));
    }

    @Test
    @DisplayName("Countdowns of an hour or more show hours")
    public void formatRemaining() {
        Assertions.assertEquals("05:00", CountdownTimes.formatRemaining(300));
        Assertions.assertEquals("00:00", CountdownTimes.formatRemaining(-5));
        Assertions.assertEquals("59:59", CountdownTimes.formatRemaining(3599));
        Assertions.assertEquals("1:00:00", CountdownTimes.formatRemaining(3600));
        Assertions.assertEquals("2:30:05", CountdownTimes.formatRemaining(9005));
    }
}
