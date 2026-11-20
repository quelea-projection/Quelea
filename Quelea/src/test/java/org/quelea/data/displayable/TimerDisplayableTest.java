package org.quelea.data.displayable;

import java.nio.file.Files;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.ThemeDTO;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Countdown timer")
public class TimerDisplayableTest {
    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-timer-test").toString());
        }
    }

    @Test
    public void formatsDurationsAndPreservesNamedTimer() {
        TimerDisplayable timer = new TimerDisplayable("Welcome", 90, "Starts in", "Ready", ThemeDTO.DEFAULT_THEME);
        Assertions.assertEquals("Welcome", timer.getName());
        Assertions.assertEquals("1:30", timer.secondsToTime(90));
        Assertions.assertEquals("0:09", timer.secondsToTime(9));
        Assertions.assertEquals("00:00", timer.secondsToTime(-1));
        Assertions.assertEquals("Starts in", timer.getPretext());
        Assertions.assertEquals("Ready", timer.getPosttext());
    }

    @Test
    public void unnamedTimerHasADurationBasedName() {
        TimerDisplayable timer = new TimerDisplayable("", 90, "", "", ThemeDTO.DEFAULT_THEME);
        Assertions.assertTrue(timer.getName().contains("1:30"));
        timer.setName(null);
        Assertions.assertTrue(timer.getName().contains("1:30"));
    }

    @Test
    public void unnamedClockTimerShowsTheActualHourAndMinute() {
        LocalTime finish = LocalTime.of(14, 7);
        TimerDisplayable timer = new TimerDisplayable("", finish, "", "", ThemeDTO.DEFAULT_THEME);
        Assertions.assertTrue(timer.getName().contains("14:07"), timer.getName());
    }
}
