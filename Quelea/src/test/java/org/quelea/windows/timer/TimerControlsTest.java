package org.quelea.windows.timer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.main.widgets.Timer;

@DisplayName("Countdown timer controls")
public class TimerControlsTest {
    @Test
    public void startingAndResettingControlsUpdatesTheTimer() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            Timer timer = new Timer(90, "Starts ", " now");
            TimerControls controls = new TimerControls();
            try {
                controls.addTimer(timer);
                Assertions.assertEquals("Starts 01:30 now", timer.getText());
                Assertions.assertFalse(controls.status());
                controls.play(false);
                Assertions.assertTrue(controls.status());
                controls.reset();
                Assertions.assertFalse(controls.status());
                Assertions.assertEquals("", timer.getText());
                controls.play(false);
                Assertions.assertEquals("Starts 01:30 now", timer.getText());
            } finally {
                timer.stop();
            }
            return null;
        });
    }

    @Test
    public void repeatingPlayKeepsThePlayingStateAfterDisableAndEnable() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            TimerControls controls = new TimerControls();
            controls.play(false);
            controls.play(false);
            Assertions.assertTrue(controls.status());
            controls.setDisableControls(true);
            Assertions.assertFalse(controls.status());
            controls.setDisableControls(false);
            Assertions.assertTrue(controls.status());
            return null;
        });
    }
}
